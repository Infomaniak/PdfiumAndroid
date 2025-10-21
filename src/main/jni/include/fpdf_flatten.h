/*
 * Infomaniak PDFium - Android
 * Copyright (C) 2014 Foxit Software Inc. http://www.foxitsoftware.com
 * Copyright (C) 2014 The PDFium Authors
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
#ifndef PUBLIC_FPDF_FLATTEN_H_
#define PUBLIC_FPDF_FLATTEN_H_

// NOLINTNEXTLINE(build/include)
#include "fpdfview.h"

// Flatten operation failed.
#define FLATTEN_FAIL 0
// Flatten operation succeed.
#define FLATTEN_SUCCESS 1
// Nothing to be flattened.
#define FLATTEN_NOTHINGTODO 2

// Flatten for normal display.
#define FLAT_NORMALDISPLAY 0
// Flatten for print.
#define FLAT_PRINT 1

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

// Flatten annotations and form fields into the page contents.
//
//   page  - handle to the page.
//   nFlag - One of the |FLAT_*| values denoting the page usage.
//
// Returns one of the |FLATTEN_*| values.
//
// Currently, all failures return |FLATTEN_FAIL| with no indication of the
// cause.
FPDF_EXPORT int FPDF_CALLCONV FPDFPage_Flatten(FPDF_PAGE page, int nFlag);

#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus

#endif  // PUBLIC_FPDF_FLATTEN_H_
