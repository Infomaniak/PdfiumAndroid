# Pdfium Android binding with Bitmap rendering
Uses pdfium library [from AOSP](https://android.googlesource.com/platform/external/pdfium/)

The demo app (for not modified lib) is [here](https://github.com/mshockwave/PdfiumAndroid-Demo-App)

Forked for use with [AndroidPdfViewer](https://github.com/Infomaniak/android-pdfview/) project.

API is highly compatible with original version, only additional methods were created.

# This repo was forked from [PdfiumAndroid](https://github.com/barteksc/PdfiumAndroid)

The purpose is to remove old support libraries so we no longer need to use jetifier.

It will be used with the forked [AndroidPdFViewer](https://github.com/Infomaniak/android-pdfview/)

Link to the [changelog](https://github.com/Infomaniak/PdfiumAndroid/blob/master/CHANGELOG.md)

## Installation
Add to _build.gradle_:

`compile 'com.github.mhiew:pdfium-android:1.9.2'`

Library is available in jcenter and Maven Central repositories.

## Methods inconsistency
Version 1.8.0 added method for getting page size - `PdfiumCore#getPageSize(...)`.
It is important to note, that this method does not require page to be opened. However, there are also
old `PdfiumCore#getPageWidth(...)`, `PdfiumCore#getPageWidthPoint(...)`, `PdfiumCore#getPageHeight()`
and `PdfiumCore#getPageHeightPoint()` which require page to be opened.

This inconsistency will be resolved in next major version, which aims to redesign API.

## Reading links
Version 1.8.0 introduces `PdfiumCore#getPageLinks(PdfDocument, int)` method, which allows to get list
of links from given page. Links are returned as `List` of type `PdfDocument.Link`.
`PdfDocument.Link` holds destination page (may be null), action URI (may be null or empty)
and link bounds in document page coordinates. To map page coordinates to screen coordinates you may use
`PdfiumCore#mapRectToDevice(...)`. See `PdfiumCore#mapPageCoordsToDevice(...)` for parameters description.

Sample usage:
``` java
PdfiumCore core = ...;
PdfDocument document = ...;
int pageIndex = 0;
core.openPage(document, pageIndex);
List<PdfDocument.Link> links = core.getPageLinks(document, pageIndex);
for (PdfDocument.Link link : links) {
    RectF mappedRect = core.mapRectToDevice(document, pageIndex, ..., link.getBounds())

    if (clickedArea(mappedRect)) {
        String uri = link.getUri();
        if (link.getDestPageIdx() != null) {
            // jump to page
        } else if (uri != null && !uri.isEmpty()) {
            // open URI using Intent
        }
    }
}

```

## Simple example
``` java
void openPdf() {
    ImageView iv = (ImageView) findViewById(R.id.imageView);
    ParcelFileDescriptor fd = ...;
    int pageNum = 0;
    PdfiumCore pdfiumCore = new PdfiumCore(context);
    try {
        PdfDocument pdfDocument = pdfiumCore.newDocument(fd);

        pdfiumCore.openPage(pdfDocument, pageNum);

        int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNum);
        int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNum);

        // ARGB_8888 - best quality, high memory usage, higher possibility of OutOfMemoryError
        // RGB_565 - little worse quality, twice less memory usage
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.RGB_565);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, pageNum, 0, 0,
                width, height);
        //if you need to render annotations and form fields, you can use
        //the same method above adding 'true' as last param

        iv.setImageBitmap(bitmap);

        printInfo(pdfiumCore, pdfDocument);

        pdfiumCore.closeDocument(pdfDocument); // important!
    } catch(IOException ex) {
        ex.printStackTrace();
    }
}

public void printInfo(PdfiumCore core, PdfDocument doc) {
    PdfDocument.Meta meta = core.getDocumentMeta(doc);
    Log.e(TAG, "title = " + meta.getTitle());
    Log.e(TAG, "author = " + meta.getAuthor());
    Log.e(TAG, "subject = " + meta.getSubject());
    Log.e(TAG, "keywords = " + meta.getKeywords());
    Log.e(TAG, "creator = " + meta.getCreator());
    Log.e(TAG, "producer = " + meta.getProducer());
    Log.e(TAG, "creationDate = " + meta.getCreationDate());
    Log.e(TAG, "modDate = " + meta.getModDate());

    printBookmarksTree(core.getTableOfContents(doc), "-");

}

public void printBookmarksTree(List<PdfDocument.Bookmark> tree, String sep) {
    for (PdfDocument.Bookmark b : tree) {

        Log.e(TAG, String.format("%s %s, p %d", sep, b.getTitle(), b.getPageIdx()));

        if (b.hasChildren()) {
            printBookmarksTree(b.getChildren(), sep + "-");
        }
    }
}

```
## Build native part
Go to `PROJECT_PATH/src/main/jni` and run command `$ ndk-build`.
This step may be executed only once, every future `.aar` build will use generated libs.
