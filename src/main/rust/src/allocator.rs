use std::mem;

/// Allocates memory of `size` bytes.
/// Returns a pointer to the allocated memory.
pub fn alloc(size: usize) -> *mut u8 {
    let mut buf = Vec::with_capacity(size);
    let ptr = buf.as_mut_ptr();
    mem::forget(buf);
    ptr
}

/// Deallocates memory at `ptr` with `size` bytes.
///
/// # Safety
///
/// The `ptr` must have been allocated by `alloc` with the same `size`.
pub unsafe fn dealloc(ptr: *mut u8, size: usize) {
    let _ = Vec::from_raw_parts(ptr, 0, size);
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn test_alloc_and_write() {
        let size = 100;
        let ptr = alloc(size);

        // Test 1: Pointer should not be null
        assert!(!ptr.is_null(), "Allocated pointer is null");

        // Test 2: Should be able to write to the memory
        unsafe {
            let slice = std::slice::from_raw_parts_mut(ptr, size);
            for i in 0..size {
                slice[i] = (i % 255) as u8;
            }

            // Verify written data
            for i in 0..size {
                assert_eq!(slice[i], (i % 255) as u8);
            }

            // Clean up (should not panic)
            dealloc(ptr, size);
        }
    }
}
