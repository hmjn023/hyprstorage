#![allow(static_mut_refs)]
use crate::node;

#[derive(Clone, Copy)]
#[repr(C)]
pub struct TransferInstruction {
    pub source_node_id: u32,
    pub target_node_id: u32,
    pub item_id: u32,
    pub nbt_hash: u64,
    pub quantity: u64,
}

#[derive(Clone, Copy)]
#[repr(C)]
pub struct SupplyEntry {
    pub source_node_id: u32,
    pub channel_id: u32,
    pub item_id: u32,
    pub nbt_hash: u64,
    pub quantity: u64,
}

static mut TRANSFER_BUFFER: Vec<TransferInstruction> = Vec::new();
static mut SUPPLY_BUFFER: Vec<SupplyEntry> = Vec::new();

pub fn init() {
    unsafe {
        TRANSFER_BUFFER.clear();
        SUPPLY_BUFFER.clear();
    }
}

pub fn push_supply(source_node_id: u32, channel_id: u32, item_id: u32, nbt_hash: u64, quantity: u64) {
    unsafe {
        SUPPLY_BUFFER.push(SupplyEntry {
            source_node_id,
            channel_id,
            item_id,
            nbt_hash,
            quantity,
        });
    }
}

pub fn get_transfer_buffer_size() -> usize {
    unsafe { TRANSFER_BUFFER.len() }
}

pub fn get_transfer_buffer_ptr() -> *const TransferInstruction {
    unsafe { TRANSFER_BUFFER.as_ptr() }
}

pub fn clear_transfer_buffer() {
    unsafe {
        TRANSFER_BUFFER.clear();
    }
}

pub fn tick_transport() {
    node::decrement_sleep_ticks();

    unsafe {
        if SUPPLY_BUFFER.is_empty() {
            return;
        }

        for supply in SUPPLY_BUFFER.iter() {
            let exporters = node::get_exporters_for_channel(supply.channel_id);
            if exporters.is_empty() {
                continue;
            }

            // Find exporter with highest priority
            let mut best_exporter = exporters[0];
            let mut max_priority = node::get_node_priority(best_exporter);

            for &exporter in exporters.iter().skip(1) {
                let priority = node::get_node_priority(exporter);
                if priority > max_priority {
                    max_priority = priority;
                    best_exporter = exporter;
                }
            }
            
            // Push to transfer buffer
            if TRANSFER_BUFFER.len() < 1024 {
                TRANSFER_BUFFER.push(TransferInstruction {
                    source_node_id: supply.source_node_id,
                    target_node_id: best_exporter,
                    item_id: supply.item_id,
                    nbt_hash: supply.nbt_hash,
                    quantity: supply.quantity,
                });
            } else {
                // Overflow handling: stop processing further supplies
                log::warn!("Transfer buffer overflow");
                break;
            }
        }
        
        SUPPLY_BUFFER.clear();
    }
}
