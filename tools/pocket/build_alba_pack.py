# SPDX-License-Identifier: Apache-2.0
"""Build the evaluated Alba fixture without downloads, model conversion or audio edits."""
import argparse
import hashlib
import json
from pathlib import Path, PurePosixPath
import zipfile

def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('model_zip',type=Path)
    parser.add_argument('alba_wav',type=Path)
    parser.add_argument('output_zip',type=Path)
    args=parser.parse_args()
    for path,expected in [(args.model_zip,'e45e5d0b70dadc9ab9f86f30ae519d79dbfd8adf013d42826af9205545cfdcb3'),
                          (args.alba_wav,'46264e83cb99115c3d210260e029117566d9c64f20266d10daa78107759ede3e')]:
        with path.open('rb') as stream:
            if hashlib.file_digest(stream,'sha256').hexdigest()!=expected:
                parser.error(f'Unexpected input SHA-256: {path.name}')
    if args.output_zip.exists():parser.error('Output exists; choose a new filename')
    with zipfile.ZipFile(args.model_zip) as source:
        manifest=json.loads(source.read('manifest.json'))
        assert manifest['id']=='german-fp32'
        manifest['voices'].append({'id':'alba-mackenna-casual','name':'Alba-MacKenna-casual','file':'alba-mackenna-casual.wav'})
        with zipfile.ZipFile(args.output_zip,'x',zipfile.ZIP_DEFLATED) as target:
            def add(name,data):
                info=zipfile.ZipInfo(name,date_time=(2026,10,14,0,0,0))
                info.compress_type=zipfile.ZIP_DEFLATED
                target.writestr(info,data)
            for entry in source.infolist():
                if entry.is_dir():continue
                name=PurePosixPath(entry.filename)
                if name.is_absolute() or '..' in name.parts:raise ValueError('Unsafe archive path')
                if entry.filename!='manifest.json':add(entry.filename,source.read(entry))
            add('manifest.json',json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8'))
            add('voices/alba-mackenna-casual.wav',args.alba_wav.read_bytes())
            add('ALBA_ATTRIBUTION.txt',b'''Alba voice performance by Alba MacKenna, CC BY 4.0.
https://creativecommons.org/licenses/by/4.0/
https://huggingface.co/kyutai/tts-voices/blob/323332d33f997de8394f24a193e1a76df720e01a/alba-mackenna/casual.wav
The reference recording and German model weights are unchanged. The manifest adds
the stable Alba ID for the evaluated Pocket Android engine. Generated speech is
synthetic; no endorsement by the performer or Kyutai is implied. Preserve the
original model/voice notices and model use conditions included in this pack.
''')
    with args.output_zip.open('rb') as stream:
        print(hashlib.file_digest(stream,'sha256').hexdigest(),args.output_zip.name)

if __name__=='__main__':main()
