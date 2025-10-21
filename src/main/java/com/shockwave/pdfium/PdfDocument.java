/*
 * Infomaniak PDFium - Android
 * Copyright (C) 2025 Infomaniak Network SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shockwave.pdfium;

import android.graphics.RectF;
import android.os.ParcelFileDescriptor;

import androidx.collection.ArrayMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PdfDocument {

    public static class Meta {
        String title;
        String author;
        String subject;
        String keywords;
        String creator;
        String producer;
        String creationDate;
        String modDate;

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public String getSubject() {
            return subject;
        }

        public String getKeywords() {
            return keywords;
        }

        public String getCreator() {
            return creator;
        }

        public String getProducer() {
            return producer;
        }

        public String getCreationDate() {
            return creationDate;
        }

        public String getModDate() {
            return modDate;
        }
    }

    public static class Bookmark {
        private List<Bookmark> children = new ArrayList<>();
        String title;
        long pageIdx;
        long mNativePtr;

        public List<Bookmark> getChildren() {
            return children;
        }

        public boolean hasChildren() {
            return !children.isEmpty();
        }

        public String getTitle() {
            return title;
        }

        public long getPageIdx() {
            return pageIdx;
        }
    }

    public static class Link {
        private RectF bounds;
        private Integer destPageIdx;
        private String uri;

        public Link(RectF bounds, Integer destPageIdx, String uri) {
            this.bounds = bounds;
            this.destPageIdx = destPageIdx;
            this.uri = uri;
        }

        public Integer getDestPageIdx() {
            return destPageIdx;
        }

        public String getUri() {
            return uri;
        }

        public RectF getBounds() {
            return bounds;
        }
    }

    /*package*/ PdfDocument() {
    }

    /*package*/ long mNativeDocPtr;
    /*package*/ ParcelFileDescriptor parcelFileDescriptor;

    /*package*/ final Map<Integer, Long> mNativePagesPtr = new ArrayMap<>();

    public boolean hasPage(int index) {
        return mNativePagesPtr.containsKey(index);
    }
}
