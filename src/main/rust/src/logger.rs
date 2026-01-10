extern "C" {
    /// External log function provided by the host (Kotlin/Chicory).
    pub fn wasm_log(
        level: i32,
        file_ptr: *const u8,
        file_len: i32,
        line: i32,
        msg_ptr: *const u8,
        msg_len: i32,
    );
}
