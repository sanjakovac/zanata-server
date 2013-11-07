/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.keys;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Event.NativePreviewHandler;

/**
 * Wrapper around {@link com.google.gwt.user.client.Event} and related classes,
 * allowing mocking for testing.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 */
public interface EventWrapper {
    HandlerRegistration addNativePreviewHandler(
            final NativePreviewHandler handler);

    int keyDownEvent();

    int keyUpEvent();

    /**
     * Wrapper for {@link NativePreviewEvent#getTypeInt()}, to present a
     * non-final method to allow mocking.
     *
     * See:
     * http://stackoverflow.com/questions/7210171/easymock-mocked-object-is-
     * calling-actual-method
     *
     * @param evt
     * @return
     */
    int getTypeInt(NativePreviewEvent evt);

    String getType(NativeEvent evt);

    Keys createKeys(NativeEvent evt);
}
