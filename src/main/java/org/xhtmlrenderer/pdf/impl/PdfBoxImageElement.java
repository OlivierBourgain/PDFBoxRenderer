/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.xhtmlrenderer.pdf.impl;

import java.awt.Point;
import java.awt.Rectangle;

import org.xhtmlrenderer.extend.FSImage;
import org.xhtmlrenderer.extend.ReplacedElement;
import org.xhtmlrenderer.layout.LayoutContext;
import org.xhtmlrenderer.render.BlockBox;
import org.xhtmlrenderer.render.RenderingContext;

public class PdfBoxImageElement implements PdfBoxReplacedElement {

	private FSImage image;
	private Point location = new Point(0, 0);

	public PdfBoxImageElement(FSImage image) {
		this.image = image;
	}

	@Override
	public void paint(RenderingContext c, PdfBoxOutputDevice outputDevice, BlockBox box) {
		Rectangle contentBounds = box.getContentAreaEdge(box.getAbsX(), box.getAbsY(), c);
		ReplacedElement element = box.getReplacedElement();
		outputDevice.drawImage(((PdfBoxImageElement) element).getImage(), contentBounds.x, contentBounds.y);
	}

	@Override
	public int getIntrinsicWidth() {
		return image.getWidth();
	}

	@Override
	public int getIntrinsicHeight() {
		return image.getHeight();
	}

	@Override
	public Point getLocation() {
		return location;
	}

	@Override
	public void setLocation(int x, int y) {
		location = new Point(x, y);
	}

	public FSImage getImage() {
		return image;
	}

	@Override
	public void detach(LayoutContext c) {
	}

	@Override
	public boolean isRequiresInteractivePaint() {
		return false;
	}

	@Override
	public int getBaseline() {
		return 0;
	}

	@Override
	public boolean hasBaseline() {
		return false;
	}
}
