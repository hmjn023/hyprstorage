#![allow(static_mut_refs)]

// Structure of Arrays for inventory management
static mut ITEM_IDS: Vec<u32> = Vec::new();
static mut NBT_HASHES: Vec<u64> = Vec::new();
static mut QUANTITIES: Vec<u64> = Vec::new();
static mut LOCATION_IDS: Vec<u32> = Vec::new();

pub fn init() {
    log::info!("Inventory initialized in Wasm");
    unsafe {
        ITEM_IDS.clear();
        NBT_HASHES.clear();
        QUANTITIES.clear();
        LOCATION_IDS.clear();
    }
}

pub fn get_snapshot() -> (Vec<u32>, Vec<u64>, Vec<u64>, Vec<u32>) {
    unsafe {
        (
            ITEM_IDS.clone(),
            NBT_HASHES.clone(),
            QUANTITIES.clone(),
            LOCATION_IDS.clone(),
        )
    }
}

pub fn restore_snapshot(
    item_ids: Vec<u32>,
    nbt_hashes: Vec<u64>,
    quantities: Vec<u64>,
    location_ids: Vec<u32>,
) {
    unsafe {
        ITEM_IDS = item_ids;
        NBT_HASHES = nbt_hashes;
        QUANTITIES = quantities;
        LOCATION_IDS = location_ids;
    }
}

pub fn add(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    unsafe {
        // Find existing item with same item_id, nbt_hash, and location_id
        for i in 0..ITEM_IDS.len() {
            if ITEM_IDS[i] == item_id && NBT_HASHES[i] == nbt_hash && LOCATION_IDS[i] == location_id
            {
                // Update existing item
                QUANTITIES[i] += quantity;
                return QUANTITIES[i];
            }
        }

        // Add new item
        ITEM_IDS.push(item_id);
        NBT_HASHES.push(nbt_hash);
        QUANTITIES.push(quantity);
        LOCATION_IDS.push(location_id);

        quantity
    }
}

pub fn remove(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    unsafe {
        for i in 0..ITEM_IDS.len() {
            if ITEM_IDS[i] == item_id && NBT_HASHES[i] == nbt_hash && LOCATION_IDS[i] == location_id
            {
                if QUANTITIES[i] <= quantity {
                    // Remove entire stack
                    // Use swap_remove for O(1) if order doesn't matter, but current lib.rs used remove (O(n))
                    // Maintaining order is usually safer if index stability matters, but here IDs are SoA.
                    // We stick to remove to match exact behavior of lib.rs for now.
                    ITEM_IDS.remove(i);
                    NBT_HASHES.remove(i);
                    QUANTITIES.remove(i);
                    LOCATION_IDS.remove(i);
                    return 0;
                } else {
                    // Reduce quantity
                    QUANTITIES[i] -= quantity;
                    return QUANTITIES[i];
                }
            }
        }

        // Item not found
        0
    }
}

pub fn count_item(item_id: u32, nbt_hash: u64) -> u64 {
    unsafe {
        let mut total = 0u64;
        for i in 0..ITEM_IDS.len() {
            if ITEM_IDS[i] == item_id && NBT_HASHES[i] == nbt_hash {
                total += QUANTITIES[i];
            }
        }
        total
    }
}

pub fn count_location(location_id: u32) -> u64 {
    unsafe {
        let mut total = 0u64;
        for i in 0..LOCATION_IDS.len() {
            if LOCATION_IDS[i] == location_id {
                total += QUANTITIES[i];
            }
        }
        total
    }
}

pub fn count_unique() -> u32 {
    unsafe { ITEM_IDS.len() as u32 }
}

pub fn clear_location(location_id: u32) -> u32 {
    unsafe {
        let mut removed = 0u32;
        let mut i = 0;
        while i < LOCATION_IDS.len() {
            if LOCATION_IDS[i] == location_id {
                ITEM_IDS.remove(i);
                NBT_HASHES.remove(i);
                QUANTITIES.remove(i);
                LOCATION_IDS.remove(i);
                removed += 1;
            } else {
                i += 1;
            }
        }
        removed
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn setup() {
        init();
    }

    #[test]
    fn test_add_and_count() {
        setup();
        // Add item
        let total = add(1, 100, 10, 1);
        assert_eq!(total, 10);

        // Count specific item
        assert_eq!(count_item(1, 100), 10);

        // Add more of same
        let total = add(1, 100, 5, 1);
        assert_eq!(total, 15);
        assert_eq!(count_item(1, 100), 15);

        // Add different item
        add(2, 200, 20, 1);
        assert_eq!(count_item(2, 200), 20);

        // Check unique count
        assert_eq!(count_unique(), 2);
    }

    #[test]
    fn test_remove() {
        setup();
        add(1, 100, 10, 1);

        // Remove partial
        let remaining = remove(1, 100, 4, 1);
        assert_eq!(remaining, 6);
        assert_eq!(count_item(1, 100), 6);

        // Remove all
        let remaining = remove(1, 100, 6, 1);
        assert_eq!(remaining, 0);
        assert_eq!(count_item(1, 100), 0);

        // Unique count should decrease (implementation detail: usually removes from vector)
        // If we remove the entry completely
        assert_eq!(count_unique(), 0);
    }

    #[test]
    fn test_location_management() {
        setup();
        add(1, 100, 10, 1); // Loc 1
        add(1, 100, 20, 2); // Loc 2

        // Location counts
        assert_eq!(count_location(1), 10);
        assert_eq!(count_location(2), 20);

        // Total item count (all locations)
        assert_eq!(count_item(1, 100), 30);

        // Clear location
        let removed = clear_location(1);
        assert_eq!(removed, 1); // 1 stack removed
        assert_eq!(count_location(1), 0);
        assert_eq!(count_location(2), 20);
    }
}
