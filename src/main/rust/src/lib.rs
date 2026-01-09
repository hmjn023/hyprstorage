pub mod allocator;
use std::mem;
use std::collections::HashMap;

// Structure of Arrays for inventory management
static mut ITEM_IDS: Vec<u32> = Vec::new();
static mut NBT_HASHES: Vec<u64> = Vec::new();
static mut QUANTITIES: Vec<u64> = Vec::new();
static mut LOCATION_IDS: Vec<u32> = Vec::new();

// Simple test function
#[no_mangle]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}

// Initialize inventory system
#[no_mangle]
pub extern "C" fn init_inventory() {
    unsafe {
        ITEM_IDS.clear();
        NBT_HASHES.clear();
        QUANTITIES.clear();
        LOCATION_IDS.clear();
    }
}

// Add or update an item in the inventory
// Returns the new quantity
#[no_mangle]
pub extern "C" fn add_item(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    unsafe {
        // Find existing item with same item_id, nbt_hash, and location_id
        for i in 0..ITEM_IDS.len() {
            if ITEM_IDS[i] == item_id 
                && NBT_HASHES[i] == nbt_hash 
                && LOCATION_IDS[i] == location_id {
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

// Remove items from inventory
// Returns the remaining quantity (0 if all removed)
#[no_mangle]
pub extern "C" fn remove_item(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    unsafe {
        for i in 0..ITEM_IDS.len() {
            if ITEM_IDS[i] == item_id 
                && NBT_HASHES[i] == nbt_hash 
                && LOCATION_IDS[i] == location_id {
                
                if QUANTITIES[i] <= quantity {
                    // Remove entire stack
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

// Get total quantity of a specific item across all locations
#[no_mangle]
pub extern "C" fn get_item_count(item_id: u32, nbt_hash: u64) -> u64 {
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

// Get total quantity at a specific location
#[no_mangle]
pub extern "C" fn get_location_count(location_id: u32) -> u64 {
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

// Get total number of unique item types
#[no_mangle]
pub extern "C" fn get_unique_item_count() -> u32 {
    unsafe {
        ITEM_IDS.len() as u32
    }
}

// Clear all items at a specific location
#[no_mangle]
pub extern "C" fn clear_location(location_id: u32) -> u32 {
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

// Memory management functions
// #[no_mangle]
// pub extern "C" fn alloc(size: usize) -> *mut u8 {
//     let mut buf = Vec::with_capacity(size);
//     let ptr = buf.as_mut_ptr();
//     mem::forget(buf);
//     ptr
// }

// #[no_mangle]
// pub extern "C" fn dealloc(ptr: *mut u8, size: usize) {
//     unsafe {
//         let _ = Vec::from_raw_parts(ptr, 0, size);
//     }
// }
