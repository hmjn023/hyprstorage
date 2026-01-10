use log::{Level, Log, Metadata, Record};

extern "C" {
    /// External log function provided by the host (Kotlin/Chicory).
    fn wasm_log(
        level: i32,
        file_ptr: *const u8,
        file_len: i32,
        line: i32,
        msg_ptr: *const u8,
        msg_len: i32,
    );
}

pub struct WasmLogger;

impl Log for WasmLogger {
    fn enabled(&self, _metadata: &Metadata) -> bool {
        // We enable all logs for now, filtering can be done on the host side
        true
    }

    fn log(&self, record: &Record) {
        if self.enabled(record.metadata()) {
            let level = match record.level() {
                Level::Error => 1,
                Level::Warn => 2,
                Level::Info => 3,
                Level::Debug => 4,
                Level::Trace => 5,
            };

            let file = record.file().unwrap_or("");
            let line = record.line().unwrap_or(0) as i32;
            let msg = format!("{}", record.args());

            unsafe {
                wasm_log(
                    level,
                    file.as_ptr(),
                    file.len() as i32,
                    line,
                    msg.as_ptr(),
                    msg.len() as i32,
                );
            }
        }
    }

    fn flush(&self) {}
}