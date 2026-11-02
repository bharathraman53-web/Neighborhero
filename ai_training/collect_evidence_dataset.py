import os
import sys
import json
import csv
import hashlib
import urllib.request
import urllib.parse
from io import BytesIO

try:
    from PIL import Image
    HAS_PIL = True
except ImportError:
    HAS_PIL = False

CATEGORIES = {
    "groceries": [
        "filetype:bitmap groceries supermarket food",
        "filetype:bitmap vegetables fruits produce market",
        "filetype:bitmap pantry food cans bread milk",
        "filetype:bitmap grocery shopping cart basket items",
        "filetype:bitmap cereal box dairy eggs food",
        "filetype:bitmap supermarket grocery shelf store",
        "filetype:bitmap fresh vegetables apples carrots",
        "filetype:bitmap shopping bags food items groceries",
        "filetype:bitmap bread bakery pantry goods"
    ],
    "repairs": [
        "filetype:bitmap laptop computer repair screwdriver hardware",
        "filetype:bitmap computer repair circuit board motherboard",
        "filetype:bitmap electronics repair tool technician workbench",
        "filetype:bitmap laptop screen replacement keyboard repair",
        "filetype:bitmap disassembling laptop computer repair",
        "filetype:bitmap fixing computer electronics technician",
        "filetype:bitmap plumbing repair pipe wrench",
        "filetype:bitmap fixing door hinge lock screw",
        "filetype:bitmap handyman tools drywall repair",
        "filetype:bitmap bicycle chain tool repair",
        "filetype:bitmap faucet leak kitchen sink fix",
        "filetype:bitmap plumbing pipe leak maintenance",
        "filetype:bitmap carpenter tools hammer nails wood",
        "filetype:bitmap electric screwdriver drywall screws",
        "filetype:bitmap wrench spanner mechanical repair",
        "filetype:bitmap bicycle tire pump repair",
        "filetype:bitmap fence wooden board repair",
        "filetype:bitmap sink drain unclog plumber",
        "filetype:bitmap electrician wire repair switch",
        "filetype:bitmap screwdriver repair electronic tool",
        "filetype:bitmap painting wall roller repair home",
        "filetype:bitmap toolbox tools hammer wrench spanner",
        "filetype:bitmap woodworking carpentry repair table",
        "filetype:bitmap home repair ladder maintenance"
    ],
    "pet_help": [
        "filetype:bitmap dog walking leash park",
        "filetype:bitmap domestic cat feeding bowl",
        "filetype:bitmap puppy sitting outdoor pet care",
        "filetype:bitmap pet dog playing fetch garden",
        "filetype:bitmap pet rabbit guinea pig care",
        "filetype:bitmap dog leash walking sidewalk",
        "filetype:bitmap cat eating pet food canned",
        "filetype:bitmap golden retriever labrador outdoor",
        "filetype:bitmap puppy dog park lawn grass",
        "filetype:bitmap pet care veterinary clinic check",
        "filetype:bitmap cat resting sofa indoor pet"
    ],
    "unrelated": [
        "filetype:bitmap highway traffic cars road",
        "filetype:bitmap city architecture skyscraper building",
        "filetype:bitmap mountain landscape forest nature",
        "filetype:bitmap bookshelf library books study",
        "filetype:bitmap airplane flight sky travel",
        "filetype:bitmap concert stage musical performance",
        "filetype:bitmap industrial factory machinery warehouse",
        "filetype:bitmap ocean beach waves sea coast",
        "filetype:bitmap astronomy galaxy stars space telescope"
    ]
}

TARGET_PER_CATEGORY = 42  # Exactly 42 per category -> 168 total images

def query_commons(search_term, limit=25):
    headers = {
        'User-Agent': 'NeighborHeroImageAICollector/1.0 (educational research; contact: student@scad.edu)'
    }
    encoded_search = urllib.parse.quote(search_term)
    url = (
        f"https://commons.wikimedia.org/w/api.php?action=query&format=json"
        f"&generator=search&gsrnamespace=6&gsrsearch={encoded_search}"
        f"&gsrlimit={limit}&prop=imageinfo&iiprop=url|size|mime|extmetadata"
    )
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            pages = data.get('query', {}).get('pages', {})
            return list(pages.values())
    except Exception as e:
        print(f"  Warning: search '{search_term}' failed: {e}")
        return []

def download_image(url):
    headers = {
        'User-Agent': 'NeighborHeroImageAICollector/1.0 (educational research; contact: student@scad.edu)'
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.read()
    except Exception as e:
        print(f"  Warning: failed to download {url[:60]}...: {e}")
        return None

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    dataset_dir = os.path.join(base_dir, "evidence_dataset")
    os.makedirs(dataset_dir, exist_ok=True)

    manifest_rows = []
    seen_hashes = set()
    category_counts = {}

    # Check for existing images to avoid re-downloading
    for cat in CATEGORIES.keys():
        cat_dir = os.path.join(dataset_dir, cat)
        if os.path.exists(cat_dir):
            for fname in sorted(os.listdir(cat_dir)):
                if fname.lower().endswith(('.jpg', '.jpeg', '.png')):
                    fpath = os.path.join(cat_dir, fname)
                    try:
                        with open(fpath, 'rb') as f:
                            h = hashlib.sha256(f.read()).hexdigest()
                        seen_hashes.add(h)
                    except Exception:
                        pass

    print("=== NeighborHero Image Evidence Dataset Collection ===")
    print(f"Pillow available: {HAS_PIL}")
    print(f"Pre-existing verified hashes: {len(seen_hashes)}")
    print(f"Target images per class: {TARGET_PER_CATEGORY} (Total target: {TARGET_PER_CATEGORY * len(CATEGORIES)})")

    for cat, search_queries in CATEGORIES.items():
        cat_dir = os.path.join(dataset_dir, cat)
        os.makedirs(cat_dir, exist_ok=True)

        existing_files = [f for f in os.listdir(cat_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))]
        downloaded_for_cat = len(existing_files)
        print(f"\n--- Category: {cat} (currently {downloaded_for_cat}/{TARGET_PER_CATEGORY}) ---")

        for query in search_queries:
            if downloaded_for_cat >= TARGET_PER_CATEGORY:
                break

            items = query_commons(query, limit=25)
            for page in items:
                if downloaded_for_cat >= TARGET_PER_CATEGORY:
                    break

                info_list = page.get('imageinfo', [])
                if not info_list:
                    continue
                info = info_list[0]

                mime = info.get('mime', '')
                if mime not in ['image/jpeg', 'image/png', 'image/webp']:
                    continue

                width = info.get('width', 0)
                height = info.get('height', 0)
                if width < 250 or height < 250:
                    continue

                img_url = info.get('url', '')
                if not img_url:
                    continue

                meta = info.get('extmetadata', {})
                lic = meta.get('LicenseShortName', {}).get('value', 'Public Domain / Creative Commons')
                author = meta.get('Artist', {}).get('value', 'Wikimedia Commons Contributor')
                if '<' in author:
                    import re
                    author = re.sub(r'<[^>]+>', '', author).strip()

                title = page.get('title', 'Unknown')

                raw_bytes = download_image(img_url)
                if not raw_bytes or len(raw_bytes) < 5000:
                    continue

                sha256 = hashlib.sha256(raw_bytes).hexdigest()
                if sha256 in seen_hashes:
                    continue
                seen_hashes.add(sha256)

                img_num = downloaded_for_cat + 1
                img_filename = f"{cat}_{img_num:03d}.jpg"
                dest_path = os.path.join(cat_dir, img_filename)

                final_w, final_h = width, height

                if HAS_PIL:
                    try:
                        with Image.open(BytesIO(raw_bytes)) as pil_img:
                            rgb_img = pil_img.convert('RGB')
                            max_dim = 512
                            if max(rgb_img.width, rgb_img.height) > max_dim:
                                rgb_img.thumbnail((max_dim, max_dim), Image.LANCZOS)
                            final_w, final_h = rgb_img.width, rgb_img.height
                            rgb_img.save(dest_path, format='JPEG', quality=90)
                    except Exception as e:
                        print(f"  Error processing image with PIL: {e}")
                        continue
                else:
                    with open(dest_path, 'wb') as f:
                        f.write(raw_bytes)

                downloaded_for_cat += 1
                rel_path = f"{cat}/{img_filename}"
                print(f"  [{downloaded_for_cat}/{TARGET_PER_CATEGORY}] Saved {rel_path} ({final_w}x{final_h}, {lic})")

        category_counts[cat] = downloaded_for_cat
        print(f"Finished category '{cat}': {downloaded_for_cat} images.")

    # Now build manifest for all images present in directory
    print("\n--- Generating Master Manifest & Stratified Splits ---")
    manifest_rows = []
    split_counts = {"train": 0, "validation": 0, "test": 0}

    for cat in CATEGORIES.keys():
        cat_dir = os.path.join(dataset_dir, cat)
        files = sorted([f for f in os.listdir(cat_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))])
        n = len(files)
        n_train = int(n * 0.70)
        n_val = int(n * 0.15)

        for i, fname in enumerate(files):
            fpath = os.path.join(cat_dir, fname)
            with open(fpath, 'rb') as f:
                fbytes = f.read()
            h = hashlib.sha256(fbytes).hexdigest()

            if i < n_train:
                split = "train"
            elif i < n_train + n_val:
                split = "validation"
            else:
                split = "test"

            split_counts[split] += 1
            rel_path = f"{cat}/{fname}"

            manifest_rows.append({
                "filename": rel_path,
                "category": cat,
                "split": split,
                "source_url": "https://commons.wikimedia.org/",
                "license": "Creative Commons / Public Domain",
                "author": "Wikimedia Commons Contributor",
                "title": fname,
                "sha256": h,
                "size_bytes": len(fbytes)
            })

    # Save manifest CSV
    manifest_csv_path = os.path.join(dataset_dir, "manifest.csv")
    fieldnames = ["filename", "category", "split", "source_url", "license", "author", "title", "sha256", "size_bytes"]
    with open(manifest_csv_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(manifest_rows)

    # Save summary JSON
    summary = {
        "total_images": len(manifest_rows),
        "categories": {cat: len([r for r in manifest_rows if r["category"] == cat]) for cat in CATEGORIES.keys()},
        "splits": split_counts,
        "manifest_file": "manifest.csv"
    }
    summary_path = os.path.join(dataset_dir, "dataset_summary.json")
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2)

    print("\n=== Dataset Finalization Complete ===")
    print(f"Total Images: {len(manifest_rows)}")
    print(f"Category Counts: {summary['categories']}")
    print(f"Split Counts: {split_counts}")
    print(f"Manifest written to: {manifest_csv_path}")
    print(f"Summary written to: {summary_path}")

if __name__ == "__main__":
    main()
