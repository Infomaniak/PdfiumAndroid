/*
 * Infomaniak PDFium - Android
 * Copyright (C) 2017 The PDFium Authors
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
#ifndef PUBLIC_FPDF_CATALOG_H_
#define PUBLIC_FPDF_CATALOG_H_

// NOLINTNEXTLINE(build/include)
#include "fpdfview.h"

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

// Experimental API.
//
// Determine if |document| represents a tagged PDF.
//
// For the definition of tagged PDF, See (see 10.7 "Tagged PDF" in PDF
// Reference 1.7).
//
//   document - handle to a document.
//
// Returns |true| iff |document| is a tagged PDF.
FPDF_EXPORT FPDF_BOOL FPDF_CALLCONV
FPDFCatalog_IsTagged(FPDF_DOCUMENT document);

// Experimental API.
// Sets the language of |document| to |language|.
//
// document - handle to a document.
// language - the language to set to.
//
// Returns TRUE on success.
FPDF_EXPORT FPDF_BOOL FPDF_CALLCONV
FPDFCatalog_SetLanguage(FPDF_DOCUMENT document, FPDF_BYTESTRING language);

#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus

#endif  // PUBLIC_FPDF_CATALOG_H_
