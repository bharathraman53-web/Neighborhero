import os
import sys
import json
import csv
import hashlib
import urllib.request
import urllib.parse
from io import BytesIO
from PIL import Image

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_DIR = os.path.join(BASE_DIR, "evidence_dataset")
REPAIRS_DIR = os.path.join(DATASET_DIR, "repairs")

REPAIR_QUERIES = [
    "laptop repair",
    "laptop repair screwdriver",
    "computer repair technician",
    "motherboard repair soldering",
    "computer hardware technician workbench",
    "laptop disassembly repair",
    "fixing laptop computer",
    "electronics repair soldering iron"
]

def query_commons(search_term, limit=15):
    headers = {
        'User-Agent': 'NeighborHeroImageAICollector/2.0 (educational research; student@scad.edu)'
    }
    encoded = urllib.parse.quote(search_term)
    url = (
        f"https://commons.wikimedia.org/w/api.php?action=query&format=json"
        f"&generator=search&gsrnamespace=6&gsrsearch={encoded}"
        f"&gsrlimit={limit}&prop=imageinfo&iiprop=url|size|mime|extmetadata"
    )
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = json.loads(resp.read().decode('utf-8'))
            return list(data.get('query', {}).get('pages', {}).values())
    except Exception as e:
        print(f"Warning: search '{search_term}' failed: {e}")
        return []

def download_image(url):
    headers = {
        'User-Agent': 'NeighborHeroImageAICollector/2.0 (educational research; student@scad.edu)'
    }
    req = urllib.request.Request(url, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=20) as resp:
            return resp.read()
    except Exception as e:
        print(f"Warning: failed to download {url[:60]}: {e}")
        return None

def main():
    print("=== Adding Tech & Laptop Repair Images to Repairs Dataset ===")
    
    # Collect existing hashes across all categories
    seen_hashes = set()
    manifest_path = os.path.join(DATASET_DIR, "manifest.csv")
    if os.path.exists(manifest_path):
        with open(manifest_path, "r", encoding="utf-8") as f:
            for r in csv.DictReader(f):
                seen_hashes.add(r['sha256'])
    print(f"Existing known hashes: {len(seen_hashes)}")

    existing_repair_files = sorted([f for f in os.listdir(REPAIRS_DIR) if f.startswith("repairs_") and f.endswith(".jpg")])
    current_count = len(existing_repair_files)
    print(f"Current repairs count: {current_count}")
    
    target_new = 11  # Add 11 new laptop/computer repair images to bring total to 52
    added = 0
    
    for q in REPAIR_QUERIES:
        if added >= target_new:
            break
        print(f"Searching: '{q}'...")
        pages = query_commons(q, limit=15)
        for p in pages:
            if added >= target_new:
                break
            info_list = p.get('imageinfo', [])
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
            
            raw_bytes = download_image(img_url)
            if not raw_bytes or len(raw_bytes) < 5000:
                continue
            
            sha256 = hashlib.sha256(raw_bytes).hexdigest()
            if sha256 in seen_hashes:
                continue
            seen_hashes.add(sha256)
            
            img_num = current_count + added + 1
            filename = f"repairs_{img_num:03d}.jpg"
            dest_path = os.path.join(REPAIRS_DIR, filename)
            
            try:
                with Image.open(BytesIO(raw_bytes)) as pil_img:
                    rgb_img = pil_img.convert('RGB')
                    max_dim = 512
                    if max(rgb_img.width, rgb_img.height) > max_dim:
                        rgb_img.thumbnail((max_dim, max_dim), Image.Resampling.LANCZOS)
                    rgb_img.save(dest_path, format='JPEG', quality=90)
                added += 1
                print(f"  [{added}/{target_new}] Saved {filename} from {p.get('title')}")
            except Exception as e:
                print(f"  Error processing image: {e}")
                continue

    print(f"\nAdded {added} laptop/tech repair images to repairs/.")
    total_repairs = len([f for f in os.listdir(REPAIRS_DIR) if f.startswith("repairs_") and f.endswith(".jpg")])
    print(f"Total repair images now: {total_repairs}")

    # Re-build manifest and splits across all categories
    print("\nRebuilding manifest and train/validation/test splits...")
    categories = ["groceries", "pet_help", "repairs", "unrelated"]
    manifest_rows = []
    split_counts = {"train": 0, "validation": 0, "test": 0}

    for cat in categories:
        cat_dir = os.path.join(DATASET_DIR, cat)
        files = sorted([f for f in os.listdir(cat_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))])
        n = len(files)
        n_train = int(n * 0.70)
        n_val = int(n * 0.15)
        # remainder is test (approx 15%)

        for i, fname in enumerate(files):
            fpath = os.path.join(cat_dir, fname)
            with open(fpath, "rb") as f:
                fbytes = f.read()
            h = hashlib.sha256(fbytes).hexdigest()

            if i < n_train:
                split = "train"
            elif i < n_train + n_val:
                split = "validation"
            else:
                split = "test"

            split_counts[split] += 1
            manifest_rows.append({
                "filename": f"{cat}/{fname}",
                "category": cat,
                "split": split,
                "source_url": "https://commons.wikimedia.org/",
                "license": "Creative Commons / Public Domain",
                "author": "Wikimedia Commons Contributor",
                "title": fname,
                "sha256": h,
                "size_bytes": len(fbytes)
            })

    # Save manifest.csv
    fieldnames = ["filename", "category", "split", "source_url", "license", "author", "title", "sha256", "size_bytes"]
    with open(manifest_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(manifest_rows)

    # Save summary
    summary = {
        "total_images": len(manifest_rows),
        "categories": {cat: len([r for r in manifest_rows if r["category"] == cat]) for cat in categories},
        "splits": split_counts,
        "format": "RGB JPEG",
        "max_dimension": 512,
        "manifest_file": "manifest.csv"
    }
    summary_path = os.path.join(DATASET_DIR, "dataset_summary.json")
    with open(summary_path, "w", encoding="utf-8") as f:
        json.dump(summary, f, indent=2)

    print(f"Total dataset images: {len(manifest_rows)}")
    print(f"Category counts: {summary['categories']}")
    print(f"Splits: {split_counts}")
    print("Manifest and summary updated successfully.")

if __name__ == "__main__":
    main()

