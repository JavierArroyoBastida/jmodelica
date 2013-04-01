/*
    Copyright (C) 2009 Modelon AB

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, version 3 of the License.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package org.jmodelica.ide.outline;

import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.jmodelica.ide.helpers.hooks.IASTEditor;

public class SourceOutlineView extends OutlineView {

	@Override
	protected IContentOutlinePage getOutlinePage(IASTEditor part) {
		return part.getSourceOutlinePage();
	}

}
