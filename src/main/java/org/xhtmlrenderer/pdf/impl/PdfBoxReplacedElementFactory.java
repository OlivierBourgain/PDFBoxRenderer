package org.xhtmlrenderer.pdf.impl;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public class PdfBoxReplacedElementFactory implements ReplacedElementFactory {
	private static final int DOTS_PER_PIXEL = 20;

	public PdfBoxReplacedElementFactory(PdfBoxOutputDevice _outputDevice) {
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box, UserAgentCallback uac, int cssWidth, int cssHeight) {
		Element e = box.getElement();
		if (e == null) {
			return null;
		}

		String nodeName = e.getNodeName();
		if (nodeName.equals("img")) {
			String srcAttr = e.getAttribute("src");
			if (srcAttr != null && srcAttr.length() > 0) {
				FSImage fsImage = uac.getImageResource(srcAttr).getImage();
				if (fsImage != null) {
					if (cssWidth != -1 || cssHeight != -1) {
						fsImage.scale(cssWidth / DOTS_PER_PIXEL, cssHeight / DOTS_PER_PIXEL);
					}
					return new PdfBoxImageElement(fsImage);
				}
			}

		}
		return null;
	}

	@Override
	public void reset() {

	}

	@Override
	public void remove(Element e) {

	}

	@Override
	public void setFormSubmissionListener(FormSubmissionListener listener) {

	}

}
