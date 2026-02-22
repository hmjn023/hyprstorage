#![allow(static_mut_refs)]

// Structure of Arrays for node management
static mut NEXT_NODE_ID: u32 = 1;

static mut NODE_IDS: Vec<u32> = Vec::new();
static mut NODE_TYPES: Vec<u8> = Vec::new();
static mut NODE_ACTIVE: Vec<u8> = Vec::new();
static mut X_COORDS: Vec<i32> = Vec::new();
static mut Y_COORDS: Vec<i32> = Vec::new();
static mut Z_COORDS: Vec<i32> = Vec::new();

// Node connection/channel properties
static mut NODE_CHANNELS: Vec<u32> = Vec::new();
static mut NODE_PRIORITIES: Vec<i8> = Vec::new();
static mut NODE_SLEEP_TICKS: Vec<u32> = Vec::new(); // 0 means active
static mut NODE_BACKOFF_LVL: Vec<u8> = Vec::new(); // level of exponential backoff

pub fn init() {
    log::info!("Node manager initialized in Wasm");
    unsafe {
        NEXT_NODE_ID = 1;
        NODE_IDS.clear();
        NODE_TYPES.clear();
        NODE_ACTIVE.clear();
        X_COORDS.clear();
        Y_COORDS.clear();
        Z_COORDS.clear();
        NODE_CHANNELS.clear();
        NODE_PRIORITIES.clear();
        NODE_SLEEP_TICKS.clear();
        NODE_BACKOFF_LVL.clear();
    }
}

pub fn register_node(x: i32, y: i32, z: i32, node_type: u8) -> u32 {
    unsafe {
        let id = NEXT_NODE_ID;
        NEXT_NODE_ID += 1;
        
        NODE_IDS.push(id);
        NODE_TYPES.push(node_type);
        NODE_ACTIVE.push(1); // Default to active (1)
        X_COORDS.push(x);
        Y_COORDS.push(y);
        Z_COORDS.push(z);
        // Default channel: 0, priority: 0
        NODE_CHANNELS.push(0);
        NODE_PRIORITIES.push(0);
        NODE_SLEEP_TICKS.push(0);
        NODE_BACKOFF_LVL.push(0);
        
        id
    }
}

pub fn unregister_node(node_id: u32) {
    unsafe {
        if let Some(index) = NODE_IDS.iter().position(|&id| id == node_id) {
            // Remove node from all vectors
            NODE_IDS.remove(index);
            NODE_TYPES.remove(index);
            NODE_ACTIVE.remove(index);
            X_COORDS.remove(index);
            Y_COORDS.remove(index);
            Z_COORDS.remove(index);
            NODE_CHANNELS.remove(index);
            NODE_PRIORITIES.remove(index);
            NODE_SLEEP_TICKS.remove(index);
            NODE_BACKOFF_LVL.remove(index);
        }
    }
}

pub fn set_node_active(node_id: u32, active: u8) {
    unsafe {
        if let Some(index) = NODE_IDS.iter().position(|&id| id == node_id) {
            NODE_ACTIVE[index] = active;
        }
    }
}

pub fn count_nodes() -> usize {
    unsafe {
        NODE_IDS.len()
    }
}

pub fn set_node_sleep(node_id: u32, ticks: u32, backoff_lvl: u8) {
    unsafe {
        if let Some(index) = NODE_IDS.iter().position(|&id| id == node_id) {
            NODE_SLEEP_TICKS[index] = ticks;
            NODE_BACKOFF_LVL[index] = backoff_lvl;
        }
    }
}

pub fn decrement_sleep_ticks() {
    unsafe {
        for ticks in NODE_SLEEP_TICKS.iter_mut() {
            if *ticks > 0 {
                *ticks -= 1;
            }
        }
    }
}

pub fn get_exporters_for_channel(channel_id: u32) -> Vec<u32> {
    let mut exporters = Vec::new();
    unsafe {
        for i in 0..NODE_IDS.len() {
            if NODE_TYPES[i] == 1 && NODE_ACTIVE[i] == 1 && NODE_CHANNELS[i] == channel_id && NODE_SLEEP_TICKS[i] == 0 {
                exporters.push(NODE_IDS[i]);
            }
        }
    }
    exporters
}

pub fn get_node_priority(node_id: u32) -> i8 {
    unsafe {
        if let Some(index) = NODE_IDS.iter().position(|&id| id == node_id) {
            NODE_PRIORITIES[index]
        } else {
            0
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    fn setup() {
        init();
    }

    #[test]
    fn test_node_lifecycle() {
        setup();
        
        // Register Importer
        let node1 = register_node(10, 20, 30, 0);
        assert_eq!(node1, 1);
        assert_eq!(count_nodes(), 1);
        
        // Register Exporter
        let node2 = register_node(11, 21, 31, 1);
        assert_eq!(node2, 2);
        assert_eq!(count_nodes(), 2);
        
        // Disable node1
        set_node_active(node1, 0);
        unsafe {
            let idx = NODE_IDS.iter().position(|&id| id == node1).unwrap();
            assert_eq!(NODE_ACTIVE[idx], 0);
        }
        
        // Unregister node1
        unregister_node(node1);
        assert_eq!(count_nodes(), 1);
        
        unsafe {
            assert_eq!(NODE_IDS[0], node2);
            assert_eq!(NODE_TYPES[0], 1);
        }
    }
}
