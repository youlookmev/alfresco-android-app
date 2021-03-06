/*******************************************************************************
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco Mobile for Android.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package org.alfresco.mobile.android.platform.extensions;

import org.alfresco.mobile.android.platform.Manager;
import org.alfresco.mobile.android.platform.data.DocumentTypeRecord;

import android.content.Context;

public abstract class SamsungManager extends Manager
{
    protected static final Object LOCK = new Object();

    protected static Manager mInstance;

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTANTS
    // ///////////////////////////////////////////////////////////////////////////
    public static final String SAMSUNG_NOTE_EXTENSION_SPD = "spd";

    public static final String SAMSUNG_NOTE_MIMETYPE = "application/samsung_note";

    // ///////////////////////////////////////////////////////////////////////////
    // CONSTRUCTOR
    // ///////////////////////////////////////////////////////////////////////////
    public static SamsungManager getInstance(Context context)
    {
        synchronized (LOCK)
        {
            if (mInstance == null)
            {
                mInstance = Manager.getInstance(context, SamsungManager.class.getSimpleName());
            }

            return (SamsungManager) mInstance;
        }
    }

    protected SamsungManager(Context applicationContext)
    {
        super(applicationContext);
    }

    // ///////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////////////////////////////////////////////////
    public abstract DocumentTypeRecord addDocumentTypeRecord();

    public abstract boolean hasPenEnable();

}
