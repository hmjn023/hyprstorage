use crate::inventory;
use crc32fast::Hasher;
use serde::{Deserialize, Serialize};

const MAGIC: u32 = 0x48535031;
const VERSION: u32 = 1;
const HEADER_SIZE: usize = 32;

#[derive(Serialize, Deserialize)]
struct SnapshotData {
    item_ids: Vec<u32>,
    nbt_hashes: Vec<u64>,
    quantities: Vec<u64>,
    location_ids: Vec<u32>,
}

pub fn create_snapshot() -> Vec<u8> {
    let mut data = Vec::new();

    let (item_ids, nbt_hashes, quantities, location_ids) = inventory::get_snapshot();

    let snapshot_data = SnapshotData {
        item_ids,
        nbt_hashes,
        quantities,
        location_ids,
    };

    let soa_bytes = bincode::serialize(&snapshot_data).unwrap_or_default();
    let length = soa_bytes.len() as u32;

    let mut hasher = Hasher::new();
    hasher.update(&soa_bytes);
    let crc = hasher.finalize();

    let timestamp: u64 = 0;
    let reserved: u64 = 0;

    data.extend_from_slice(&MAGIC.to_le_bytes());
    data.extend_from_slice(&VERSION.to_le_bytes());
    data.extend_from_slice(&length.to_le_bytes());
    data.extend_from_slice(&crc.to_le_bytes());
    data.extend_from_slice(&timestamp.to_le_bytes());
    data.extend_from_slice(&reserved.to_le_bytes());

    data.extend_from_slice(&soa_bytes);

    data
}

pub fn restore_snapshot(data: &[u8]) -> bool {
    if data.len() < HEADER_SIZE {
        return false;
    }

    let magic = u32::from_le_bytes(data[0..4].try_into().unwrap());
    if magic != MAGIC {
        return false;
    }

    let version = u32::from_le_bytes(data[4..8].try_into().unwrap());
    if version != VERSION {
        return false;
    }

    let length = u32::from_le_bytes(data[8..12].try_into().unwrap()) as usize;
    if data.len() < HEADER_SIZE + length {
        return false;
    }

    let expected_crc = u32::from_le_bytes(data[12..16].try_into().unwrap());
    
    let payload = &data[HEADER_SIZE..HEADER_SIZE + length];
    let mut hasher = Hasher::new();
    hasher.update(payload);
    let crc = hasher.finalize();

    if crc != expected_crc {
        return false;
    }

    let snapshot_data: SnapshotData = match bincode::deserialize(payload) {
        Ok(data) => data,
        Err(_) => return false,
    };

    inventory::restore_snapshot(
        snapshot_data.item_ids,
        snapshot_data.nbt_hashes,
        snapshot_data.quantities,
        snapshot_data.location_ids,
    );

    true
}

#[cfg(test)]
mod tests {
    use super::*;
    use crate::inventory;

    #[test]
    fn test_create_and_restore_snapshot() {
        inventory::init();
        inventory::add(1, 100, 10, 1);
        inventory::add(2, 200, 20, 2);

        let snapshot = create_snapshot();
        assert!(!snapshot.is_empty(), "Snapshot should not be empty");

        // Clear inventory to test restore
        inventory::init();
        assert_eq!(inventory::count_item(1, 100), 0);

        // Restore snapshot
        let restored = restore_snapshot(&snapshot);
        assert!(restored, "Snapshot restore should succeed");

        // Verify restored data
        assert_eq!(inventory::count_item(1, 100), 10);
        assert_eq!(inventory::count_item(2, 200), 20);
    }

    #[test]
    fn test_restore_invalid_snapshot() {
        // Test empty
        assert!(!restore_snapshot(&[]));

        // Test bad magic
        let mut bad_magic = create_snapshot();
        bad_magic[0] = 0x00;
        assert!(!restore_snapshot(&bad_magic));

        // Test corrupted payload
        let mut corrupted = create_snapshot();
        let len = corrupted.len();
        corrupted[len - 1] ^= 0xFF; // Flip a bit in the payload
        assert!(!restore_snapshot(&corrupted));
    }
}
