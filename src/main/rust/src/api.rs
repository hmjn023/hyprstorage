use crate::allocator;
use crate::inventory;

#[no_mangle]
pub extern "C" fn alloc(size: usize) -> *mut u8 {
    allocator::alloc(size)
}

#[no_mangle]
pub extern "C" fn dealloc(ptr: *mut u8, size: usize) {
    allocator::dealloc(ptr, size)
}

#[no_mangle]
pub extern "C" fn init_inventory() {
    inventory::init()
}

#[no_mangle]
pub extern "C" fn add_item(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    inventory::add(item_id, nbt_hash, quantity, location_id)
}

#[no_mangle]
pub extern "C" fn remove_item(item_id: u32, nbt_hash: u64, quantity: u64, location_id: u32) -> u64 {
    inventory::remove(item_id, nbt_hash, quantity, location_id)
}

#[no_mangle]
pub extern "C" fn get_item_count(item_id: u32, nbt_hash: u64) -> u64 {
    inventory::count_item(item_id, nbt_hash)
}

#[no_mangle]
pub extern "C" fn get_location_count(location_id: u32) -> u64 {
    inventory::count_location(location_id)
}

#[no_mangle]
pub extern "C" fn get_unique_item_count() -> u32 {
    inventory::count_unique()
}

#[no_mangle]
pub extern "C" fn clear_location(location_id: u32) -> u32 {
    inventory::clear_location(location_id)
}

#[no_mangle]
pub extern "C" fn add(a: i32, b: i32) -> i32 {
    a + b
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_api_alloc() {
        let size = 10;
        let ptr = alloc(size);
        assert!(!ptr.is_null());
        dealloc(ptr, size);
    }
    
    #[test]
    fn test_api_inventory() {
        init_inventory();
        let qty = add_item(1, 0, 10, 1);
        // assert_eq!(qty, 10); // Red Phase: expect failure
        // Actually, if I return 0, it might be equal to 0 if expected is 0.
        // But add_item should return new quantity.
        assert_eq!(qty, 10);
    }
}
