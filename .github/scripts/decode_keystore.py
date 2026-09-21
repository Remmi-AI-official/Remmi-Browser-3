#!/usr/bin/env python3
import os
import sys
import base64

def main():
    workspace = os.environ.get("WORKSPACE_PATH", ".")
    b64_data = os.environ.get("KEYSTORE_BASE64", "")
    valid_chars = set("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=")
    clean_b64 = "".join(c for c in b64_data if c in valid_chars)
    
    if not clean_b64:
        sys.stderr.write("::error::REMMI_RELEASE_KEYSTORE_B64 is empty or contains no valid base64 characters.\n")
        sys.exit(1)
        
    out_path = os.path.join(workspace, "release.keystore")
    try:
        raw_bytes = base64.b64decode(clean_b64)
        with open(out_path, "wb") as f:
            f.write(raw_bytes)
    except Exception as exc:
        sys.stderr.write(f"::error::Failed to decode keystore base64: {exc}\n")
        sys.exit(1)

if __name__ == "__main__":
    main()
