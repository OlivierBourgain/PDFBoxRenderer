package org.xhtmlrenderer.pdf.impl;

import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public interface PdfBoxReplacedElement extends ReplacedElement {
	public void paint(RenderingContext c, PdfBoxOutputDevice outputDevice, BlockBox box);
}
