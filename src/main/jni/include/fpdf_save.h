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
#ifndef PUBLIC_FPDF_SAVE_H_
#define PUBLIC_FPDF_SAVE_H_

// clang-format off
// NOLINTNEXTLINE(build/include)
#include "fpdfview.h"

#ifdef __cplusplus
extern "C" {
#endif

// Structure for custom file write
typedef struct FPDF_FILEWRITE_ {
  //
  // Version number of the interface. Currently must be 1.
  //
  int version;

  // Method: WriteBlock
  //          Output a block of data in your custom way.
  // Interface Version:
  //          1
  // Implementation Required:
  //          Yes
  // Comments:
  //          Called by function FPDF_SaveDocument
  // Parameters:
  //          pThis       -   Pointer to the structure itself
  //          pData       -   Pointer to a buffer to output
  //          size        -   The size of the buffer.
  // Return value:
  //          Should be non-zero if successful, zero for error.
  int (*WriteBlock)(struct FPDF_FILEWRITE_* pThis,
                    const void* pData,
                    unsigned long size);
} FPDF_FILEWRITE;

 // Flags for FPDF_SaveAsCopy()
#define FPDF_INCREMENTAL 1
#define FPDF_NO_INCREMENTAL 2
#define FPDF_REMOVE_SECURITY 3

// Function: FPDF_SaveAsCopy
//          Saves the copy of specified document in custom way.
// Parameters:
//          document        -   Handle to document, as returned by
//                              FPDF_LoadDocument() or FPDF_CreateNewDocument().
//          pFileWrite      -   A pointer to a custom file write structure.
//          flags           -   The creating flags.
// Return value:
//          TRUE for succeed, FALSE for failed.
//
FPDF_EXPORT FPDF_BOOL FPDF_CALLCONV FPDF_SaveAsCopy(FPDF_DOCUMENT document,
                                                    FPDF_FILEWRITE* pFileWrite,
                                                    FPDF_DWORD flags);

// Function: FPDF_SaveWithVersion
//          Same as FPDF_SaveAsCopy(), except the file version of the
//          saved document can be specified by the caller.
// Parameters:
//          document        -   Handle to document.
//          pFileWrite      -   A pointer to a custom file write structure.
//          flags           -   The creating flags.
//          fileVersion     -   The PDF file version. File version: 14 for 1.4,
//                              15 for 1.5, ...
// Return value:
//          TRUE if succeed, FALSE if failed.
//
FPDF_EXPORT FPDF_BOOL FPDF_CALLCONV
FPDF_SaveWithVersion(FPDF_DOCUMENT document,
                     FPDF_FILEWRITE* pFileWrite,
                     FPDF_DWORD flags,
                     int fileVersion);

#ifdef __cplusplus
}
#endif

#endif  // PUBLIC_FPDF_SAVE_H_
