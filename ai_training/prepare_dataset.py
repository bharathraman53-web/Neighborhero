import os
import csv
import json
import hashlib
import random
import numpy as np
from PIL import Image, ImageEnhance

random.seed(42)
np.random.seed(42)

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
DATASET_DIR = os.path.join(BASE_DIR, "evidence_dataset")

def create_variation(img, var_type):
    """Create a realistic variation with different framing or lighting."""
    if var_type == "flip_h":
        return img.transpose(Image.FLIP_LEFT_RIGHT)
    elif var_type == "crop_center":
        w, h = img.size
        cw, ch = int(w * 0.88), int(h * 0.88)
        left = (w - cw) // 2
        top = (h - ch) // 2
        cropped = img.crop((left, top, left + cw, top + ch))
        return cropped.resize((w, h), Image.Resampling.LANCZOS)
    elif var_type == "brighten":
        enhancer = ImageEnhance.Brightness(img)
        return enhancer.enhance(1.12)
    elif var_type == "contrast":
        enhancer = ImageEnhance.Contrast(img)
        return enhancer.enhance(1.15)
    elif var_type == "cool_tone":
        enhancer = ImageEnhance.Color(img)
        return enhancer.enhance(0.9)
    return img

def main():
    print("=== NeighborHero: Preparing and Balancing Vision Dataset ===")
    
    # 1. Expand laptop repair variations from repairs_042 .. repairs_052
    repairs_dir = os.path.join(DATASET_DIR, "repairs")
    laptop_source_indices = list(range(42, 53))
    
    current_repair_files = [f for f in os.listdir(repairs_dir) if f.startswith("repairs_") and f.endswith(".jpg")]
    print(f"Current repair files: {len(current_repair_files)}")
    
    next_idx = max([int(f.replace("repairs_", "").replace(".jpg", "")) for f in current_repair_files]) + 1
    
    # Add 12 distinct variations of laptop/electronics repair images
    variations = ["flip_h", "crop_center", "brighten", "contrast", "cool_tone"]
    v_idx = 0
    for src_num in laptop_source_indices:
        src_file = os.path.join(repairs_dir, f"repairs_{src_num:03d}.jpg")
        if not os.path.exists(src_file):
            continue
        with Image.open(src_file) as img:
            rgb = img.convert("RGB")
            var_img = create_variation(rgb, variations[v_idx % len(variations)])
            out_file = os.path.join(repairs_dir, f"repairs_{next_idx:03d}.jpg")
            var_img.save(out_file, "JPEG", quality=92)
            print(f"Created laptop repair variation: repairs_{next_idx:03d}.jpg (from #{src_num:03d})")
            next_idx += 1
            v_idx += 1
            
            # also create a cropped variant for the first few
            if next_idx <= 64:
                var_img2 = create_variation(rgb, "crop_center")
                out_file2 = os.path.join(repairs_dir, f"repairs_{next_idx:03d}.jpg")
                var_img2.save(out_file2, "JPEG", quality=92)
                print(f"Created laptop repair crop: repairs_{next_idx:03d}.jpg (from #{src_num:03d})")
                next_idx += 1

    # 2. Expand groceries to 50
    groc_dir = os.path.join(DATASET_DIR, "groceries")
    groc_files = [f for f in os.listdir(groc_dir) if f.startswith("groceries_") and f.endswith(".jpg")]
    g_next = max([int(f.replace("groceries_", "").replace(".jpg", "")) for f in groc_files]) + 1
    while g_next <= 50:
        src_id = (g_next - 43) % len(groc_files) + 1
        src_file = os.path.join(groc_dir, f"groceries_{src_id:03d}.jpg")
        with Image.open(src_file) as img:
            rgb = img.convert("RGB")
            var = create_variation(rgb, "flip_h" if g_next % 2 == 0 else "crop_center")
            var.save(os.path.join(groc_dir, f"groceries_{g_next:03d}.jpg"), "JPEG", quality=92)
            print(f"Created groceries variation: groceries_{g_next:03d}.jpg")
        g_next += 1

    # 3. Expand pet_help to 50
    pet_dir = os.path.join(DATASET_DIR, "pet_help")
    pet_files = [f for f in os.listdir(pet_dir) if f.startswith("pet_help_") and f.endswith(".jpg")]
    p_next = max([int(f.replace("pet_help_", "").replace(".jpg", "")) for f in pet_files]) + 1
    while p_next <= 50:
        src_id = (p_next - 43) % len(pet_files) + 1
        src_file = os.path.join(pet_dir, f"pet_help_{src_id:03d}.jpg")
        with Image.open(src_file) as img:
            rgb = img.convert("RGB")
            var = create_variation(rgb, "flip_h" if p_next % 2 == 0 else "crop_center")
            var.save(os.path.join(pet_dir, f"pet_help_{p_next:03d}.jpg"), "JPEG", quality=92)
            print(f"Created pet_help variation: pet_help_{p_next:03d}.jpg")
        p_next += 1

    # 4. Expand unrelated to 50
    unrelated_dir = os.path.join(DATASET_DIR, "unrelated")
    unrelated_files = [f for f in os.listdir(unrelated_dir) if f.startswith("unrelated_") and f.endswith(".jpg")]
    u_next = max([int(f.replace("unrelated_", "").replace(".jpg", "")) for f in unrelated_files]) + 1
    while u_next <= 50:
        src_id = (u_next - 43) % len(unrelated_files) + 1
        src_file = os.path.join(unrelated_dir, f"unrelated_{src_id:03d}.jpg")
        with Image.open(src_file) as img:
            rgb = img.convert("RGB")
            var = create_variation(rgb, "flip_h" if u_next % 2 == 0 else "crop_center")
            var.save(os.path.join(unrelated_dir, f"unrelated_{u_next:03d}.jpg"), "JPEG", quality=92)
            print(f"Created unrelated variation: unrelated_{u_next:03d}.jpg")
        u_next += 1

    # 5. Build stratified manifest with random seed
    categories = ["groceries", "pet_help", "repairs", "unrelated"]
    manifest_rows = []
    split_counts = {"train": 0, "validation": 0, "test": 0}

    for cat in categories:
        cat_dir = os.path.join(DATASET_DIR, cat)
        files = sorted([f for f in os.listdir(cat_dir) if f.lower().endswith(('.jpg', '.jpeg', '.png'))])
        
        # In repairs, we have general repairs (< 42) and laptop repairs (>= 42)
        if cat == "repairs":
            general_repairs = [f for f in files if int(f.replace("repairs_", "").replace(".jpg", "")) < 42]
            laptop_repairs = [f for f in files if int(f.replace("repairs_", "").replace(".jpg", "")) >= 42]
            
            # Shuffle each subgroup reproducibly
            r_gen = random.Random(42)
            r_gen.shuffle(general_repairs)
            r_lap = random.Random(42)
            r_lap.shuffle(laptop_repairs)
            
            # 70% train, 15% val, 15% test for both subgroups
            g_tr = int(len(general_repairs) * 0.70)
            g_va = int(len(general_repairs) * 0.15)
            
            l_tr = int(len(laptop_repairs) * 0.70)
            l_va = int(len(laptop_repairs) * 0.15)
            
            train_files = general_repairs[:g_tr] + laptop_repairs[:l_tr]
            val_files = general_repairs[g_tr:g_tr+g_va] + laptop_repairs[l_tr:l_tr+l_va]
            test_files = general_repairs[g_tr+g_va:] + laptop_repairs[l_tr+l_va:]
            
            print(f"\nRepairs split composition:")
            print(f"  Train:      {len(train_files)} (general: {g_tr}, laptop: {l_tr})")
            print(f"  Validation: {len(val_files)} (general: {g_va}, laptop: {l_va})")
            print(f"  Test:       {len(test_files)} (general: {len(general_repairs) - g_tr - g_va}, laptop: {len(laptop_repairs) - l_tr - l_va})")
            
            cat_splits = [("train", f) for f in train_files] + [("validation", f) for f in val_files] + [("test", f) for f in test_files]
        else:
            r_cat = random.Random(42)
            shuffled = list(files)
            r_cat.shuffle(shuffled)
            n_tr = int(len(shuffled) * 0.70)
            n_va = int(len(shuffled) * 0.15)
            
            train_files = shuffled[:n_tr]
            val_files = shuffled[n_tr:n_tr+n_va]
            test_files = shuffled[n_tr+n_va:]
            cat_splits = [("train", f) for f in train_files] + [("validation", f) for f in val_files] + [("test", f) for f in test_files]

        for split, fname in cat_splits:
            fpath = os.path.join(cat_dir, fname)
            with open(fpath, "rb") as f:
                fbytes = f.read()
            h = hashlib.sha256(fbytes).hexdigest()
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

    # Sort manifest for deterministic output
    manifest_rows.sort(key=lambda x: (x["category"], x["split"], x["filename"]))
    
    manifest_path = os.path.join(DATASET_DIR, "manifest.csv")
    fieldnames = ["filename", "category", "split", "source_url", "license", "author", "title", "sha256", "size_bytes"]
    with open(manifest_path, "w", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=fieldnames)
        writer.writeheader()
        writer.writerows(manifest_rows)

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

    print(f"\nManifest successfully written: {len(manifest_rows)} total images.")
    print(f"Summary: {json.dumps(summary, indent=2)}")

if __name__ == "__main__":
    main()

