/*
 * Infomaniak PDFium - Android
 * Copyright (C) 2018 The PDFium Authors
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
#ifndef PUBLIC_CPP_FPDF_SCOPERS_H_
#define PUBLIC_CPP_FPDF_SCOPERS_H_

#include <memory>
#include <type_traits>

#include "fpdf_deleters.h"

// Versions of FPDF types that clean up the object at scope exit.

using ScopedFPDFAnnotation =
    std::unique_ptr<std::remove_pointer<FPDF_ANNOTATION>::type,
                    FPDFAnnotationDeleter>;

using ScopedFPDFAvail =
    std::unique_ptr<std::remove_pointer<FPDF_AVAIL>::type, FPDFAvailDeleter>;

using ScopedFPDFBitmap =
    std::unique_ptr<std::remove_pointer<FPDF_BITMAP>::type, FPDFBitmapDeleter>;

using ScopedFPDFClipPath =
    std::unique_ptr<std::remove_pointer<FPDF_CLIPPATH>::type,
                    FPDFClipPathDeleter>;

using ScopedFPDFDocument =
    std::unique_ptr<std::remove_pointer<FPDF_DOCUMENT>::type,
                    FPDFDocumentDeleter>;

using ScopedFPDFFont =
    std::unique_ptr<std::remove_pointer<FPDF_FONT>::type, FPDFFontDeleter>;

using ScopedFPDFFormHandle =
    std::unique_ptr<std::remove_pointer<FPDF_FORMHANDLE>::type,
                    FPDFFormHandleDeleter>;

using ScopedFPDFJavaScriptAction =
    std::unique_ptr<std::remove_pointer<FPDF_JAVASCRIPT_ACTION>::type,
                    FPDFJavaScriptActionDeleter>;

using ScopedFPDFPage =
    std::unique_ptr<std::remove_pointer<FPDF_PAGE>::type, FPDFPageDeleter>;

using ScopedFPDFPageLink =
    std::unique_ptr<std::remove_pointer<FPDF_PAGELINK>::type,
                    FPDFPageLinkDeleter>;

using ScopedFPDFPageObject =
    std::unique_ptr<std::remove_pointer<FPDF_PAGEOBJECT>::type,
                    FPDFPageObjectDeleter>;

using ScopedFPDFStructTree =
    std::unique_ptr<std::remove_pointer<FPDF_STRUCTTREE>::type,
                    FPDFStructTreeDeleter>;

using ScopedFPDFTextFind =
    std::unique_ptr<std::remove_pointer<FPDF_SCHHANDLE>::type,
                    FPDFTextFindDeleter>;

using ScopedFPDFTextPage =
    std::unique_ptr<std::remove_pointer<FPDF_TEXTPAGE>::type,
                    FPDFTextPageDeleter>;

#endif  // PUBLIC_CPP_FPDF_SCOPERS_H_
