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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.Surface;

import com.shockwave.pdfium.util.Size;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PdfiumCore {
    private static final String TAG = PdfiumCore.class.getName();
    private static final Class FD_CLASS = FileDescriptor.class;
    private static final String FD_FIELD_NAME = "descriptor";

    /**
     * Search flag: only match the text with the same case. The case is ignored by default.
     */
    public static final int SEARCH_MATCH_CASE = 0x00000001;

    /**
     * Search flag: only match whole words. Parts of words are matched by default.
     */
    public static final int SEARCH_MATCH_WHOLE_WORD = 0x00000002;

    /**
     * Search flag: matches can overlap, the next one is looked for right after the start of the current one instead
     * of right after its end. Searching "aa" in "aaa" finds 2 matches instead of 1.
     */
    public static final int SEARCH_CONSECUTIVE = 0x00000004;

    private static final int RELATIVE_RECT_UNIT = 10_000;

    static {
        try {
            System.loadLibrary("modpng");
            System.loadLibrary("modft2");
            System.loadLibrary("modpdfium");
            System.loadLibrary("jniPdfium");
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "Native libraries failed to load - " + e);
        }
    }

    private native long nativeOpenDocument(int fd, String password);

    private native long nativeOpenMemDocument(byte[] data, String password);

    private native void nativeCloseDocument(long docPtr);

    private native int nativeGetPageCount(long docPtr);

    private native long nativeLoadPage(long docPtr, int pageIndex);

    private native long[] nativeLoadPages(long docPtr, int fromIndex, int toIndex);

    private native void nativeClosePage(long docPtr, long pagePtr);

    private native void nativeClosePages(long docPtr, long[] pagesPtr);

    private native int nativeGetPageWidthPixel(long pagePtr, int dpi);

    private native int nativeGetPageHeightPixel(long pagePtr, int dpi);

    private native int nativeGetPageWidthPoint(long pagePtr);

    private native int nativeGetPageHeightPoint(long pagePtr);

    //private native long nativeGetNativeWindow(Surface surface);
    //private native void nativeRenderPage(long pagePtr, long nativeWindowPtr);
    private native void nativeRenderPage(long docPtr, long pagePtr, Surface surface, int dpi,
                                         int startX, int startY,
                                         int drawSizeHor, int drawSizeVer,
                                         boolean renderAnnot);

    private native void nativeRenderPageBitmap(long docPtr, long pagePtr, Bitmap bitmap, int dpi,
                                               int startX, int startY,
                                               int drawSizeHor, int drawSizeVer,
                                               boolean renderAnnot);

    private native String nativeGetDocumentMetaText(long docPtr, String tag);

    private native Long nativeGetFirstChildBookmark(long docPtr, Long bookmarkPtr);

    private native Long nativeGetSiblingBookmark(long docPtr, long bookmarkPtr);

    private native String nativeGetBookmarkTitle(long bookmarkPtr);

    private native long nativeGetBookmarkDestIndex(long docPtr, long bookmarkPtr);

    private native Size nativeGetPageSizeByIndex(long docPtr, int pageIndex, int dpi);

    private native long[] nativeGetPageLinks(long pagePtr);

    private native Integer nativeGetDestPageIndex(long docPtr, long linkPtr);

    private native String nativeGetLinkURI(long docPtr, long linkPtr);

    private native RectF nativeGetLinkRect(long linkPtr);

    private native Point nativePageCoordsToDevice(long pagePtr, int startX, int startY, int sizeX,
                                                  int sizeY, int rotate, double pageX, double pageY);

    private native PointF nativeDeviceCoordsToPage(long pagePtr, int startX, int startY, int sizeX,
                                                   int sizeY, int rotate, int deviceX, int deviceY);

    private native long nativeLoadTextPage(long pagePtr);

    private native void nativeCloseTextPage(long textPagePtr);

    private native int nativeGetPageTextCount(long textPagePtr);

    private native String nativeGetPageText(long textPagePtr, int startIndex, int count);

    private native int nativeGetCharIndexAtCoord(long textPagePtr, double pageX, double pageY,
                                                 double xTolerance, double yTolerance);

    private native RectF nativeGetCharBox(long textPagePtr, int charIndex);

    private native int[] nativeSearchText(long textPagePtr, String findWhat, int flags);

    private native float[] nativeGetTextRects(long textPagePtr, int startIndex, int count);

    /* synchronize native methods */
    private static final Object lock = new Object();
    private static Field mFdField = null;
    private int mCurrentDpi;

    public static int getNumFd(ParcelFileDescriptor fdObj) {
        try {
            if (mFdField == null) {
                mFdField = FD_CLASS.getDeclaredField(FD_FIELD_NAME);
                mFdField.setAccessible(true);
            }

            return mFdField.getInt(fdObj.getFileDescriptor());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            return -1;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return -1;
        }
    }


    /**
     * Context needed to get screen density
     */
    public PdfiumCore(Context ctx) {
        mCurrentDpi = ctx.getResources().getDisplayMetrics().densityDpi;
    }

    /**
     * Create new document from file
     */
    public PdfDocument newDocument(ParcelFileDescriptor fd) throws IOException {
        return newDocument(fd, null);
    }

    /**
     * Create new document from file with password
     */
    public PdfDocument newDocument(ParcelFileDescriptor fd, String password) throws IOException {
        PdfDocument document = new PdfDocument();
        document.parcelFileDescriptor = fd;
        synchronized (lock) {
            document.mNativeDocPtr = nativeOpenDocument(getNumFd(fd), password);
        }

        return document;
    }

    /**
     * Create new document from bytearray
     */
    public PdfDocument newDocument(byte[] data) throws IOException {
        return newDocument(data, null);
    }

    /**
     * Create new document from bytearray with password
     */
    public PdfDocument newDocument(byte[] data, String password) throws IOException {
        PdfDocument document = new PdfDocument();
        synchronized (lock) {
            document.mNativeDocPtr = nativeOpenMemDocument(data, password);
        }
        return document;
    }

    /**
     * Get total numer of pages in document
     */
    public int getPageCount(PdfDocument doc) {
        synchronized (lock) {
            return nativeGetPageCount(doc.mNativeDocPtr);
        }
    }

    /**
     * Open page and store native pointer in {@link PdfDocument}
     */
    public long openPage(PdfDocument doc, int pageIndex) {
        long pagePtr;
        synchronized (lock) {
            pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex);
            doc.mNativePagesPtr.put(pageIndex, pagePtr);
            return pagePtr;
        }

    }

    /**
     * Open range of pages and store native pointers in {@link PdfDocument}
     */
    public long[] openPage(PdfDocument doc, int fromIndex, int toIndex) {
        long[] pagesPtr;
        synchronized (lock) {
            pagesPtr = nativeLoadPages(doc.mNativeDocPtr, fromIndex, toIndex);
            int pageIndex = fromIndex;
            for (long page : pagesPtr) {
                if (pageIndex > toIndex) break;
                doc.mNativePagesPtr.put(pageIndex, page);
                pageIndex++;
            }

            return pagesPtr;
        }
    }

    /**
     * Get page width in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageWidth(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page height in pixels. <br>
     * This method requires page to be opened.
     */
    public int getPageHeight(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPixel(pagePtr, mCurrentDpi);
            }
            return 0;
        }
    }

    /**
     * Get page width in PostScript points (1/72th of an inch).<br>
     * This method requires page to be opened.
     */
    public int getPageWidthPoint(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageWidthPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get page height in PostScript points (1/72th of an inch).<br>
     * This method requires page to be opened.
     */
    public int getPageHeightPoint(PdfDocument doc, int index) {
        synchronized (lock) {
            Long pagePtr;
            if ((pagePtr = doc.mNativePagesPtr.get(index)) != null) {
                return nativeGetPageHeightPoint(pagePtr);
            }
            return 0;
        }
    }

    /**
     * Get size of page in pixels.<br>
     * This method does not require given page to be opened.
     */
    public Size getPageSize(PdfDocument doc, int index) {
        synchronized (lock) {
            return nativeGetPageSizeByIndex(doc.mNativeDocPtr, index, mCurrentDpi);
        }
    }

    /**
     * Render page fragment on {@link Surface}.<br>
     * Page must be opened before rendering.
     */
    public void renderPage(PdfDocument doc, Surface surface, int pageIndex,
                           int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPage(doc, surface, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Surface}. This method allows to render annotations.<br>
     * Page must be opened before rendering.
     */
    public void renderPage(PdfDocument doc, Surface surface, int pageIndex,
                           int startX, int startY, int drawSizeX, int drawSizeY,
                           boolean renderAnnot) {
        synchronized (lock) {
            try {
                //nativeRenderPage(doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi);
                nativeRenderPage(doc.mNativeDocPtr, doc.mNativePagesPtr.get(pageIndex), surface, mCurrentDpi,
                        startX, startY, drawSizeX, drawSizeY, renderAnnot);
            } catch (NullPointerException e) {
                Log.e(TAG, "mContext may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
    }

    /**
     * Render page fragment on {@link Bitmap}.<br>
     * Page must be opened before rendering.
     * <p>
     * Supported bitmap configurations:
     * <ul>
     * <li>ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
     * <li>RGB_565 - little worse quality, twice less memory usage
     * </ul>
     */
    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY) {
        renderPageBitmap(doc, bitmap, pageIndex, startX, startY, drawSizeX, drawSizeY, false);
    }

    /**
     * Render page fragment on {@link Bitmap}. This method allows to render annotations.<br>
     * Page must be opened before rendering.
     * <p>
     * For more info see {@link PdfiumCore#renderPageBitmap(PdfDocument, Bitmap, int, int, int, int, int)}
     */
    public void renderPageBitmap(PdfDocument doc, Bitmap bitmap, int pageIndex,
                                 int startX, int startY, int drawSizeX, int drawSizeY,
                                 boolean renderAnnot) {
        synchronized (lock) {
            try {
                nativeRenderPageBitmap(doc.mNativeDocPtr, doc.mNativePagesPtr.get(pageIndex), bitmap, mCurrentDpi,
                        startX, startY, drawSizeX, drawSizeY, renderAnnot);
            } catch (NullPointerException e) {
                Log.e(TAG, "mContext may be null");
                e.printStackTrace();
            } catch (Exception e) {
                Log.e(TAG, "Exception throw from native");
                e.printStackTrace();
            }
        }
    }

    /**
     * Release native resources and opened file
     */
    public void closeDocument(PdfDocument doc) {
        synchronized (lock) {
            for (Integer index : doc.mNativeTextPagesPtr.keySet()) {
                nativeCloseTextPage(doc.mNativeTextPagesPtr.get(index));
            }
            doc.mNativeTextPagesPtr.clear();

            for (Integer index : doc.mNativePagesPtr.keySet()) {
                nativeClosePage(doc.mNativeDocPtr, doc.mNativePagesPtr.get(index));
            }
            doc.mNativePagesPtr.clear();

            nativeCloseDocument(doc.mNativeDocPtr);

            if (doc.parcelFileDescriptor != null) { //if document was loaded from file
                try {
                    doc.parcelFileDescriptor.close();
                } catch (IOException e) {
                    /* ignore */
                }
                doc.parcelFileDescriptor = null;
            }
        }
    }

    /**
     * Get metadata for given document
     */
    public PdfDocument.Meta getDocumentMeta(PdfDocument doc) {
        synchronized (lock) {
            PdfDocument.Meta meta = new PdfDocument.Meta();
            meta.title = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Title");
            meta.author = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Author");
            meta.subject = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Subject");
            meta.keywords = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Keywords");
            meta.creator = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Creator");
            meta.producer = nativeGetDocumentMetaText(doc.mNativeDocPtr, "Producer");
            meta.creationDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "CreationDate");
            meta.modDate = nativeGetDocumentMetaText(doc.mNativeDocPtr, "ModDate");

            return meta;
        }
    }

    /**
     * Get table of contents (bookmarks) for given document
     */
    public List<PdfDocument.Bookmark> getTableOfContents(PdfDocument doc) {
        synchronized (lock) {
            List<PdfDocument.Bookmark> topLevel = new ArrayList<>();
            Long first = nativeGetFirstChildBookmark(doc.mNativeDocPtr, null);
            if (first != null) {
                recursiveGetBookmark(topLevel, doc, first);
            }
            return topLevel;
        }
    }

    private void recursiveGetBookmark(List<PdfDocument.Bookmark> tree, PdfDocument doc, long bookmarkPtr) {
        PdfDocument.Bookmark bookmark = new PdfDocument.Bookmark();
        bookmark.mNativePtr = bookmarkPtr;
        bookmark.title = nativeGetBookmarkTitle(bookmarkPtr);
        bookmark.pageIdx = nativeGetBookmarkDestIndex(doc.mNativeDocPtr, bookmarkPtr);
        tree.add(bookmark);

        Long child = nativeGetFirstChildBookmark(doc.mNativeDocPtr, bookmarkPtr);
        if (child != null) {
            recursiveGetBookmark(bookmark.getChildren(), doc, child);
        }

        Long sibling = nativeGetSiblingBookmark(doc.mNativeDocPtr, bookmarkPtr);
        if (sibling != null) {
            recursiveGetBookmark(tree, doc, sibling);
        }
    }

    /**
     * Get all links from given page
     */
    public List<PdfDocument.Link> getPageLinks(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            List<PdfDocument.Link> links = new ArrayList<>();
            Long nativePagePtr = doc.mNativePagesPtr.get(pageIndex);
            if (nativePagePtr == null) {
                return links;
            }
            long[] linkPtrs = nativeGetPageLinks(nativePagePtr);
            for (long linkPtr : linkPtrs) {
                Integer index = nativeGetDestPageIndex(doc.mNativeDocPtr, linkPtr);
                String uri = nativeGetLinkURI(doc.mNativeDocPtr, linkPtr);

                RectF rect = nativeGetLinkRect(linkPtr);
                if (rect != null && (index != null || uri != null)) {
                    links.add(new PdfDocument.Link(rect, index, uri));
                }

            }
            return links;
        }
    }

    /**
     * Map page coordinates to device screen coordinates
     *
     * @param doc       pdf document
     * @param pageIndex index of page
     * @param startX    left pixel position of the display area in device coordinates
     * @param startY    top pixel position of the display area in device coordinates
     * @param sizeX     horizontal size (in pixels) for displaying the page
     * @param sizeY     vertical size (in pixels) for displaying the page
     * @param rotate    page orientation: 0 (normal), 1 (rotated 90 degrees clockwise),
     *                  2 (rotated 180 degrees), 3 (rotated 90 degrees counter-clockwise)
     * @param pageX     X value in page coordinates
     * @param pageY     Y value in page coordinate
     * @return mapped coordinates
     */
    public Point mapPageCoordsToDevice(PdfDocument doc, int pageIndex, int startX, int startY, int sizeX,
                                       int sizeY, int rotate, double pageX, double pageY) {
        long pagePtr = doc.mNativePagesPtr.get(pageIndex);
        return nativePageCoordsToDevice(pagePtr, startX, startY, sizeX, sizeY, rotate, pageX, pageY);
    }

    /**
     * Convert the screen coordinates of a point to page coordinates.
     * <p>
     * The page coordinate system has its origin at the left-bottom corner
     * of the page, with the X-axis on the bottom going to the right, and
     * the Y-axis on the left side going up.
     * <p>
     * NOTE: this coordinate system can be altered when you zoom, scroll,
     * or rotate a page, however, a point on the page should always have
     * the same coordinate values in the page coordinate system.
     * <p>
     * The device coordinate system is device dependent. For screen device,
     * its origin is at the left-top corner of the window. However this
     * origin can be altered by the Windows coordinate transformation
     * utilities.
     * <p>
     * You must make sure the start_x, start_y, size_x, size_y
     * and rotate parameters have exactly same values as you used in
     * the FPDF_RenderPage() function call.
     *
     * @param doc       pdf document
     * @param pageIndex index of page
     * @param startX    Left pixel position of the display area in device coordinates.
     * @param startY    Top pixel position of the display area in device coordinates.
     * @param sizeX     Horizontal size (in pixels) for displaying the page.
     * @param sizeY     Vertical size (in pixels) for displaying the page.
     * @param rotate    Page orientation:
     *                  0 (normal)
     *                  1 (rotated 90 degrees clockwise)
     *                  2 (rotated 180 degrees)
     *                  3 (rotated 90 degrees counter-clockwise)
     * @param deviceX   X value in device coordinates to be converted.
     * @param deviceY   Y value in device coordinates to be converted.
     * @return
     */
    public PointF mapDeviceCoordsToPage(PdfDocument doc, int pageIndex, int startX, int startY, int sizeX,
                                        int sizeY, int rotate, int deviceX, int deviceY) {
        long pagePtr = doc.mNativePagesPtr.get(pageIndex);
        return nativeDeviceCoordsToPage(pagePtr, startX, startY, sizeX, sizeY, rotate, deviceX, deviceY);
    }

    /**
     * @return mapped coordinates
     * @see PdfiumCore#mapPageCoordsToDevice(PdfDocument, int, int, int, int, int, int, double, double)
     */
    public RectF mapRectToDevice(PdfDocument doc, int pageIndex, int startX, int startY, int sizeX,
                                 int sizeY, int rotate, RectF coords) {

        Point leftTop = mapPageCoordsToDevice(doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
                coords.left, coords.top);
        Point rightBottom = mapPageCoordsToDevice(doc, pageIndex, startX, startY, sizeX, sizeY, rotate,
                coords.right, coords.bottom);
        return new RectF(leftTop.x, leftTop.y, rightBottom.x, rightBottom.y);
    }

    /**
     * Open and cache text page for a previously opened page.
     */
    public long openTextPage(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            Long cached = doc.mNativeTextPagesPtr.get(pageIndex);
            if (cached != null) {
                return cached;
            }
            Long pagePtr = doc.mNativePagesPtr.get(pageIndex);
            if (pagePtr == null) {
                throw new IllegalStateException("Page is not opened: " + pageIndex);
            }
            long textPagePtr = nativeLoadTextPage(pagePtr);
            doc.mNativeTextPagesPtr.put(pageIndex, textPagePtr);
            return textPagePtr;
        }
    }

    /**
     * Close text page if it was opened explicitly.
     */
    public void closeTextPage(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            Long textPagePtr = doc.mNativeTextPagesPtr.remove(pageIndex);
            if (textPagePtr != null) {
                nativeCloseTextPage(textPagePtr);
            }
        }
    }

    /**
     * Count all selectable characters on a page.
     */
    public int getPageTextCount(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            return nativeGetPageTextCount(openTextPage(doc, pageIndex));
        }
    }

    /**
     * Read full page text.
     */
    public String getPageText(PdfDocument doc, int pageIndex) {
        synchronized (lock) {
            long textPagePtr = openTextPage(doc, pageIndex);
            int textCount = nativeGetPageTextCount(textPagePtr);
            if (textCount <= 0) {
                return "";
            }
            return nativeGetPageText(textPagePtr, 0, textCount);
        }
    }

    /**
     * Read a text segment from page by character range.
     */
    public String getPageText(PdfDocument doc, int pageIndex, int startIndex, int count) {
        synchronized (lock) {
            if (count <= 0) {
                return "";
            }
            return nativeGetPageText(openTextPage(doc, pageIndex), startIndex, count);
        }
    }

    /**
     * Hit-test page coordinates to a character index.
     */
    public int getCharIndexAtCoord(PdfDocument doc, int pageIndex, double pageX, double pageY,
                                   double xTolerance, double yTolerance) {
        synchronized (lock) {
            return nativeGetCharIndexAtCoord(openTextPage(doc, pageIndex), pageX, pageY, xTolerance, yTolerance);
        }
    }

    /**
     * Get bounding box for one character in page coordinates.
     */
    public RectF getCharBox(PdfDocument doc, int pageIndex, int charIndex) {
        synchronized (lock) {
            return nativeGetCharBox(openTextPage(doc, pageIndex), charIndex);
        }
    }

    /**
     * Get the areas covering a range of characters, to highlight a selection for instance.
     * Characters on the same line are merged in a single rectangle, which is lighter than calling
     * {@link #getCharBox(PdfDocument, int, int)} for each character. <br>
     * This method requires page to be opened.
     *
     * @return Rectangles in page coordinates, like {@link #getCharBox(PdfDocument, int, int)}. Use
     * {@link #mapRectToDevice(PdfDocument, int, int, int, int, int, int, RectF)} to convert them.
     */
    public List<RectF> getTextRects(PdfDocument doc, int pageIndex, int startIndex, int count) {
        synchronized (lock) {
            if (count <= 0) {
                return new ArrayList<>();
            }
            return toRectList(nativeGetTextRects(openTextPage(doc, pageIndex), startIndex, count));
        }
    }

    /**
     * Search a text in a page. <br>
     * The page doesn't have to be opened: if it isn't, it is opened for the time of the search and closed right
     * after, so a whole document can be searched page by page without keeping all the pages in memory. Like the
     * other methods, it locks the library while it runs so call it from a background thread.
     *
     * @param query Text to search, the words it is made of (separated by spaces) have to follow each other.
     * @param flags Combination of {@link #SEARCH_MATCH_CASE}, {@link #SEARCH_MATCH_WHOLE_WORD} and
     *              {@link #SEARCH_CONSECUTIVE}, or 0 to ignore the case and match parts of words.
     * @return The matches in reading order. Empty if there is none or if the query is empty.
     * @throws IllegalStateException if the page can't be loaded, if the index is not valid for instance.
     */
    public List<PdfDocument.SearchResult> searchText(PdfDocument doc, int pageIndex, String query, int flags) {
        List<PdfDocument.SearchResult> results = new ArrayList<>();
        if (query == null || query.isEmpty()) {
            return results;
        }

        synchronized (lock) {
            Long openedPagePtr = doc.mNativePagesPtr.get(pageIndex);
            boolean pageOpenedHere = openedPagePtr == null;
            long pagePtr;
            if (pageOpenedHere) {
                pagePtr = nativeLoadPage(doc.mNativeDocPtr, pageIndex);
            } else {
                pagePtr = openedPagePtr;
            }

            try {
                Long openedTextPagePtr = doc.mNativeTextPagesPtr.get(pageIndex);
                boolean textPageOpenedHere = openedTextPagePtr == null;
                long textPagePtr;
                if (textPageOpenedHere) {
                    textPagePtr = nativeLoadTextPage(pagePtr);
                } else {
                    textPagePtr = openedTextPagePtr;
                }

                try {
                    int[] matches = nativeSearchText(textPagePtr, query, flags);
                    if (matches != null) {
                        // Matches are (start index, character count) pairs
                        for (int i = 0; i + 1 < matches.length; i += 2) {
                            List<RectF> rects = toRectList(nativeGetTextRects(textPagePtr, matches[i], matches[i + 1]));
                            results.add(new PdfDocument.SearchResult(pageIndex, matches[i], matches[i + 1], rects,
                                    toRelativeRects(pagePtr, rects)));
                        }
                    }
                } finally {
                    if (textPageOpenedHere) {
                        nativeCloseTextPage(textPagePtr);
                    }
                }
            } finally {
                if (pageOpenedHere) {
                    nativeClosePage(doc.mNativeDocPtr, pagePtr);
                }
            }
        }
        return results;
    }

    /**
     * Search a text in a page, ignoring the case and matching parts of words.
     *
     * @see #searchText(PdfDocument, int, String, int)
     */
    public List<PdfDocument.SearchResult> searchText(PdfDocument doc, int pageIndex, String query) {
        return searchText(doc, pageIndex, query, 0);
    }

    /**
     * Converts areas in page coordinates to fractions of the displayed page. The page has to be opened.
     */
    private List<RectF> toRelativeRects(long pagePtr, List<RectF> rects) {
        List<RectF> relativeRects = new ArrayList<>(rects.size());
        for (RectF rect : rects) {
            // The page is mapped on a square of RELATIVE_RECT_UNIT pixels, which is the precision of the fractions
            Point topLeft = nativePageCoordsToDevice(pagePtr, 0, 0, RELATIVE_RECT_UNIT, RELATIVE_RECT_UNIT, 0,
                    rect.left, rect.top);
            Point bottomRight = nativePageCoordsToDevice(pagePtr, 0, 0, RELATIVE_RECT_UNIT, RELATIVE_RECT_UNIT, 0,
                    rect.right, rect.bottom);
            RectF mapped = new RectF(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);
            // The corners are swapped on the pages that are rotated
            mapped.sort();
            relativeRects.add(new RectF(mapped.left / RELATIVE_RECT_UNIT, mapped.top / RELATIVE_RECT_UNIT,
                    mapped.right / RELATIVE_RECT_UNIT, mapped.bottom / RELATIVE_RECT_UNIT));
        }
        return relativeRects;
    }

    private static List<RectF> toRectList(float[] values) {
        List<RectF> rects = new ArrayList<>();
        if (values != null) {
            // Rectangles are (left, top, right, bottom) quadruplets
            for (int i = 0; i + 3 < values.length; i += 4) {
                rects.add(new RectF(values[i], values[i + 1], values[i + 2], values[i + 3]));
            }
        }
        return rects;
    }
}
