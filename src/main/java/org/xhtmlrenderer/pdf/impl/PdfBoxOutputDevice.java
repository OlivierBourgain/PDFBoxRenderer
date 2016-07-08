package org.xhtmlrenderer.pdf.impl;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageContentStream.AppendMode;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.xhtmlrenderer.css.parser.FSCMYKColor;
import org.xhtmlrenderer.css.parser.FSColor;
import org.xhtmlrenderer.css.parser.FSRGBColor;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.OutputDevice;
import org.xhtmlrenderer.render.AbstractOutputDevice;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.FSFont;
import org.xhtmlrenderer.render.InlineText;
import org.xhtmlrenderer.render.JustificationInfo;
import org.xhtmlrenderer.render.RenderingContext;

public class PdfBoxOutputDevice extends AbstractOutputDevice implements OutputDevice {
	// 20 dot per px, 96 px per in et 72 pt per in
	private static final float DOTS_PER_POINT = 20f * 96 / 72;
	private static Logger log = Logger.getLogger("PDFBoxOutputDevice");

	private PDDocument doc;
	private PDPage currentpage;

	PDPageContentStream contents;
	private AffineTransform transform;
	private Area clip;

	private Color color;
	private Stroke stroke;

	private PdfBoxFSFont font;

	public void setDocument(PDDocument doc) {
		this.doc = doc;
	}

	public void initialize(PDPage page) {
		log.info("initialize page");
		this.currentpage = page;
		transform = computeTransformationMatrix(page);
		try {
			contents = new PDPageContentStream(doc, currentpage, AppendMode.APPEND, false);
		} catch (IOException e) {
			log.severe("Error opening page stream");
		}
	}

	public void finishPage(PDPage page) {
		log.info("finish page");
		try {
			contents.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Return the transformation matrix from the model space (in dots, from
	 * FS-core), to the page page (in points).
	 */
	private AffineTransform computeTransformationMatrix(PDPage page) {
		AffineTransform at = new AffineTransform();
		at.translate(0, currentpage.getMediaBox().getHeight());
		at.scale(1. / DOTS_PER_POINT, -1. / DOTS_PER_POINT);
		return at;
	}

	@Override
	public void paintReplacedElement(RenderingContext c, BlockBox box) {
		PdfBoxReplacedElement element = (PdfBoxReplacedElement) box.getReplacedElement();
		element.paint(c, this, box);

	}

	@Override
	public void setFont(FSFont font) {
		this.font = (PdfBoxFSFont) font;
	}

	@Override
	public void setColor(FSColor c) {
		if (c instanceof FSRGBColor) {
			FSRGBColor rgb = (FSRGBColor) c;
			color = new Color(rgb.getRed(), rgb.getGreen(), rgb.getBlue());
		} else if (c instanceof FSCMYKColor) {
			throw new UnsupportedOperationException("Not implemented yet!");
		} else {
			throw new RuntimeException("internal error: unsupported color class " + color.getClass().getName());
		}
	}

	@Override
	public void drawBorderLine(Shape bounds, int side, int width, boolean solid) {
		draw(bounds);
	}

	@Override
	public void drawImage(FSImage image, int x, int y) {
		PdfBoxFSImage img = (PdfBoxFSImage) image;
		log.info("drawImage at " + x + "/" + y);
		log.info("w/h = " + img.getWidth() + "/" + img.getHeight());
		try {
			PDImageXObject imgx = LosslessFactory.createFromImage(doc, img.bimg);

			Point2D src = new Point2D.Float(x, y + img.getHeight());
			Point2D dest = new Point2D.Float();
			transform.transform(src, dest);

			float width = (float) (img.getWidth() * transform.getScaleX());
			float height = (float) (-img.getHeight() * transform.getScaleY());
			contents.drawImage(imgx, (float) dest.getX(), (float) dest.getY(), width, height);

		} catch (IOException e) {
			log.severe("IO Exception " + e.getStackTrace());
		}
	}

	// -----------------------
	// DRAW
	// -----------------------
	@Override
	public void draw(Shape s) {
		log.info("draw shape " + s.getBounds2D().toString());
		followPath(s, STROKE);
	}

	@Override
	protected void drawLine(int x1, int y1, int x2, int y2) {
		Line2D line = new Line2D.Double(x1, y1, x2, y2);
		draw(line);
	}

	@Override
	public void drawRect(int x, int y, int width, int height) {
		draw(new Rectangle(x, y, width, height));
	}

	@Override
	public void drawOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
		draw(oval);
	}

	// -----------------------
	// FILL
	// -----------------------
	@Override
	public void fill(Shape s) {
		log.info("fill " + s.getBounds2D());
		followPath(s, FILL);
	}

	@Override
	public void fillRect(int x, int y, int width, int height) {
		fill(new Rectangle(x, y, width, height));
	}

	@Override
	public void fillOval(int x, int y, int width, int height) {
		Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
		fill(oval);
	}

	// ---------------------
	// Clipping
	// ---------------------
	@Override
	public void clip(Shape s) {
		log.info("Clip shape " + s);
		if (s != null) {
			Shape t = transform.createTransformedShape(s);
			if (clip == null)
				clip = new Area(t);
			else
				clip.intersect(new Area(t));
			followPath(t, CLIP);
		} else {
			throw new RuntimeException("Shape is null, unexpected");
		}
	}

	@Override
	public Shape getClip() {
		try {
			return transform.createInverse().createTransformedShape(clip);
		} catch (NoninvertibleTransformException e) {
			return null;
		}
	}

	@Override
	public void setClip(Shape s) {
		if (s == null) {
			clip = null;
		} else {
			log.info("setClip shape " + s.getBounds2D().toString());
			Shape t = transform.createTransformedShape(s);
			clip = new Area(t);
			// FIXME - Désactivé pour l'instant car ne fonctionne pas avec les
			// background color
			// followPath(t, CLIP);
		}
	}

	@Override
	public void translate(double tx, double ty) {
		transform.translate(tx, ty);
	}

	@Override
	public void setStroke(Stroke s) {
		this.stroke = transformStroke(s);
	}

	@Override
	public Stroke getStroke() {
		return stroke;
	}

	private Stroke transformStroke(Stroke s) {
		if (s == null)
			return null;
		if (!(s instanceof BasicStroke))
			throw new UnsupportedOperationException("Only Basic stroke supported " + s.getClass());

		BasicStroke st = (BasicStroke) s;
		float scale = (float) transform.getScaleX(); // In our case, scaleX =
														// scaleY
		float dash[] = st.getDashArray();
		if (dash != null) {
			for (int k = 0; k < dash.length; ++k)
				dash[k] *= scale;
		}
		return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash, st.getDashPhase() * scale);
	}

	protected void drawString(String s, float x, float y, JustificationInfo info) {
		if (s == null || s.length() == 0)
			return;
		log.info("Drawing string [" + s + "] at " + x + "/" + y);

		try {
			Point2D src = new Point2D.Float(x, y);
			Point2D dest = new Point2D.Float();
			transform.transform(src, dest);
			contents.beginText();
			contents.setNonStrokingColor(color);
			float size = (float) (font.getSize2D() * transform.getScaleX());
			contents.setFont(font.getFontDescription().getFont(), size);
			contents.newLineAtOffset((float) dest.getX(), (float) dest.getY());
			contents.showText(s);
			contents.endText();

		} catch (IOException e) {
			log.severe("IO Exception");
		}
	}

	private static final int FILL = 1;
	private static final int STROKE = 2;
	private static final int CLIP = 3;
	private static AffineTransform IDENTITY = new AffineTransform();

	// Main draw routine, copied and adapted from ITextOutputDevice.
	private void followPath(Shape s, int drawType) {
		if (s == null)
			return;

		PathIterator points;
		if (drawType == CLIP) {
			points = s.getPathIterator(IDENTITY);
		} else {
			points = s.getPathIterator(transform);
		}
		float[] coords = new float[6];
		int traces = 0;

		try {
			if (drawType == STROKE) {
				if (!(stroke instanceof BasicStroke)) {
					throw new UnsupportedOperationException("Only basic stroke supported");
				}
			}
			if (drawType == STROKE) {
				updateStroke(stroke);
				contents.setStrokingColor(color);
			} else if (drawType == FILL) {
				contents.setNonStrokingColor(color);
			}

			while (!points.isDone()) {
				traces++;
				int segtype = points.currentSegment(coords);
				switch (segtype) {
				case PathIterator.SEG_CLOSE:
					contents.closePath();
					break;

				case PathIterator.SEG_CUBICTO:
					contents.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
					break;

				case PathIterator.SEG_LINETO:
					contents.lineTo(coords[0], coords[1]);
					break;

				case PathIterator.SEG_MOVETO:
					contents.moveTo(coords[0], coords[1]);
					break;

				case PathIterator.SEG_QUADTO:
					contents.curveTo1(coords[0], coords[1], coords[2], coords[3]);
					break;
				}
				points.next();
			}

			switch (drawType) {
			case FILL:
				if (traces > 0) {
					if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
						contents.fillEvenOdd();
					else
						contents.fill();
				}
				break;
			case STROKE:
				if (traces > 0)
					contents.stroke();
				break;
			default: // drawType==CLIP
				if (traces == 0)
					contents.addRect(0, 0, 0, 0);
				if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD)
					contents.clipEvenOdd();
				else
					contents.clip();
			}

		} catch (IOException e) {
			log.severe("IO Exception" + e.getStackTrace());
		}
	}

	private void updateStroke(Stroke s) throws IOException {
		if (s == null)
			return;

		if (!(s instanceof BasicStroke))
			throw new UnsupportedOperationException("Only basicstroke supported");

		BasicStroke bs = (BasicStroke) s;
		float dash[] = bs.getDashArray();
		if (dash == null)
			contents.setLineDashPattern(new float[0], 0);
		else
			contents.setLineDashPattern(dash, bs.getDashPhase());
		contents.setLineWidth(bs.getLineWidth());
		contents.setLineJoinStyle(bs.getLineJoin());
		contents.setLineCapStyle(bs.getEndCap());
	}

	@Override
	public Object getRenderingHint(Key key) {
		// Nothing to do
		return null;
	}

	@Override
	public void setRenderingHint(Key key, Object value) {
		// Nothing to do
	}

	@Override
	public boolean isSupportsSelection() {
		return false;
	}

	@Override
	public boolean isSupportsCMYKColors() {
		return true;
	}

	@Override
	public void drawSelection(RenderingContext c, InlineText inlineText) {
		throw new UnsupportedOperationException();
	}

}
