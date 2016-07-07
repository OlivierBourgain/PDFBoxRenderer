package org.xhtmlrenderer.pdf;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.List;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xhtmlrenderer.context.StyleReference;
import org.xhtmlrenderer.css.style.CalculatedStyle;
import org.xhtmlrenderer.extend.FontResolver;
import org.xhtmlrenderer.extend.NamespaceHandler;
import org.xhtmlrenderer.extend.UserInterface;
import org.xhtmlrenderer.layout.BoxBuilder;
import org.xhtmlrenderer.layout.Layer;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.impl.PdfBoxFontResolver;
import org.xhtmlrenderer.pdf.impl.PdfBoxOutputDevice;
import org.xhtmlrenderer.pdf.impl.PdfBoxReplacedElementFactory;
import org.xhtmlrenderer.pdf.impl.PdfBoxTextRenderer;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.PageBox;
import org.xhtmlrenderer.render.RenderingContext;
import org.xhtmlrenderer.render.ViewportBox;
import org.xhtmlrenderer.resource.XMLResource;
import org.xhtmlrenderer.simple.extend.XhtmlNamespaceHandler;
import org.xhtmlrenderer.swing.NaiveUserAgent;
import org.xml.sax.InputSource;

public class PdfBoxRenderer {
	private static Logger log = Logger.getLogger("PDFBoxRenderer");
	// 20 dot per px, 96 px per in et 72 pt per in
	private static final float DOTS_PER_POINT = 20f * 96 / 72;

	private final SharedContext sharedContext;
	private Document doc;
	private BlockBox root;
	private final PdfBoxOutputDevice outputDevice;

	private static final int DEFAULT_PIXEL_PER_INCH = 96;
	private static final int DEFAULT_DOTS_PER_PIXEL = 20;
	private static final int DEFAULT_DOT_PER_INCH = DEFAULT_PIXEL_PER_INCH * DEFAULT_DOTS_PER_PIXEL;

	public PdfBoxRenderer() {
		sharedContext = new SharedContext();

		NaiveUserAgent userAgent = new NaiveUserAgent();
		sharedContext.setUserAgentCallback(userAgent);
		sharedContext.setCss(new StyleReference(userAgent));

		outputDevice = new PdfBoxOutputDevice();
		PdfBoxReplacedElementFactory replacedElementFactory = new PdfBoxReplacedElementFactory(outputDevice);
		sharedContext.setReplacedElementFactory(replacedElementFactory);

		FontResolver fontResolver = new PdfBoxFontResolver(sharedContext);
		sharedContext.setFontResolver(fontResolver);

		sharedContext.setTextRenderer(new PdfBoxTextRenderer());
		sharedContext.setDPI(DEFAULT_DOT_PER_INCH);
		sharedContext.setDotsPerPixel(DEFAULT_DOTS_PER_PIXEL);
		sharedContext.setPrint(true);
	}

	private Document loadDocument(final String uri) {
		return sharedContext.getUac().getXMLResource(uri).getDocument();
	}

	public void setDocument(String uri) {
		setDocument(loadDocument(uri), uri);
	}

	public void setDocument(Document doc, String url) {
		setDocument(doc, url, new XhtmlNamespaceHandler());
	}

	public void setDocument(File file) throws IOException {

		File parent = file.getAbsoluteFile().getParentFile();
		setDocument(loadDocument(file.toURI().toURL().toExternalForm()), (parent == null ? "" : parent.toURI().toURL().toExternalForm()));
	}

	public void setDocumentFromString(String content) {
		setDocumentFromString(content, null);
	}

	public void setDocumentFromString(String content, String baseUrl) {
		InputSource is = new InputSource(new BufferedReader(new StringReader(content)));
		Document dom = XMLResource.load(is).getDocument();

		setDocument(dom, baseUrl);
	}

	public void setDocument(Document doc, String url, NamespaceHandler nsh) {
		this.doc = doc;

		// FIXME
		// getFontResolver().flushFontFaceFonts();

		sharedContext.reset();
		sharedContext.getCss().flushAllStyleSheets();
		sharedContext.setBaseURL(url);
		sharedContext.setNamespaceHandler(nsh);
		sharedContext.getCss().setDocumentContext(sharedContext, sharedContext.getNamespaceHandler(), doc, new NullUserInterface());
		// FIXME
		// getFontResolver().importFontFaces(_sharedContext.getCss().getFontFaceRules());
	}

	private static final class NullUserInterface implements UserInterface {
		public boolean isHover(Element e) {
			return false;
		}

		public boolean isActive(Element e) {
			return false;
		}

		public boolean isFocus(Element e) {
			return false;
		}
	}

	public void layout() {
		log.info("PDFBoxRenderer.layout");
		LayoutContext c = newLayoutContext();
		root = BoxBuilder.createRootBox(c, doc);
		root.setContainingBlock(new ViewportBox(getInitialExtents(c)));
		root.layout(c);
		Dimension dim = root.getLayer().getPaintingDimension(c);
		root.getLayer().trimEmptyPages(c, dim.height);
		root.getLayer().layoutPages(c);
	}

	private Rectangle getInitialExtents(LayoutContext c) {
		PageBox first = Layer.createPageBox(c, "first");

		return new Rectangle(0, 0, first.getContentWidth(c), first.getContentHeight(c));
	}

	private LayoutContext newLayoutContext() {
		LayoutContext result = sharedContext.newLayoutContextInstance();
		// result.setFontContext(new ITextFontContext());

		sharedContext.getTextRenderer().setup(result.getFontContext());

		return result;
	}

	public void createPDF(OutputStream os) throws IOException {

		@SuppressWarnings("unchecked")
		List<PageBox> pages = root.getLayer().getPages();

		// Create a new empty document

		RenderingContext c = newRenderingContext();
		c.setInitialPageNo(1);
		c.setPageCount(pages.size());

		int pageCount = root.getLayer().getPages().size();
		root.getLayer().assignPagePaintingPositions(c, Layer.PAGED_MODE_PRINT);

		PDDocument document = new PDDocument();
		outputDevice.setDocument(document);

		for (int i = 0; i < pageCount; i++) {
			PageBox currentPage = pages.get(i);

			PDRectangle rect = new PDRectangle(0, 0, currentPage.getWidth(c) / DOTS_PER_POINT, currentPage.getHeight(c) / DOTS_PER_POINT);
			PDPage page = new PDPage(rect);
			document.addPage(page);

			outputDevice.initialize(page);

			log.info("Doing page " + i);
			c.setPage(i, currentPage);
			paintPage(c, currentPage);
			
			outputDevice.finishPage(page);
		}

		// Save the newly created document
		document.save(os);

		// finally make sure that the document is properly
		// closed.
		document.close();
	}

	private void paintPage(RenderingContext c, PageBox page) {

		page.paintBackground(c, 0, Layer.PAGED_MODE_PRINT);
		page.paintMarginAreas(c, 0, Layer.PAGED_MODE_PRINT);
		page.paintBorder(c, 0, Layer.PAGED_MODE_PRINT);

		Rectangle content = page.getPrintClippingBounds(c);
		outputDevice.clip(content);

		int top = -page.getPaintingTop() + page.getMarginBorderPadding(c, CalculatedStyle.TOP);

		int left = page.getMarginBorderPadding(c, CalculatedStyle.LEFT);

		outputDevice.translate(left, top);
		root.getLayer().paint(c);
		outputDevice.translate(-left, -top);

	}

	private RenderingContext newRenderingContext() {
		RenderingContext result = sharedContext.newRenderingContextInstance();
		result.setOutputDevice(outputDevice);
		sharedContext.getTextRenderer().setup(result.getFontContext());
		result.setRootLayer(root.getLayer());
		return result;
	}

}
