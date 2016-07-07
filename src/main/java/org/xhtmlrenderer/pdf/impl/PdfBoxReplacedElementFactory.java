package org.xhtmlrenderer.pdf.impl;

import org.w3c.dom.Element;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.extend.ReplacedElementFactory;
import org.xhtmlrenderer.extend.UserAgentCallback;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.simple.extend.FormSubmissionListener;

public class PdfBoxReplacedElementFactory implements ReplacedElementFactory {

	public PdfBoxReplacedElementFactory(PdfBoxOutputDevice _outputDevice) {
	}

	@Override
	public ReplacedElement createReplacedElement(LayoutContext c, BlockBox box,
			UserAgentCallback uac, int cssWidth, int cssHeight) {
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
