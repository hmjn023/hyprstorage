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

#[cfg(test)]
mod tests {
    use super::*;
    use log::{Level, Record};

    // Mock the external function for tests
    #[no_mangle]
    extern "C" fn wasm_log(
        level: i32,
        _file_ptr: *const u8,
        _file_len: i32,
        _line: i32,
        _msg_ptr: *const u8,
        _msg_len: i32,
    ) {
        // We can capture calls here if needed for more advanced tests
        assert!(level >= 1 && level <= 5);
    }

    #[test]
    fn test_logger_enabled() {
        let logger = WasmLogger;
        let metadata = log::Metadata::builder().level(Level::Info).build();
        assert!(logger.enabled(&metadata));
    }

    #[test]
    fn test_log_call() {
        let logger = WasmLogger;
        let record = Record::builder()
            .args(format_args!("test message"))
            .level(Level::Error)
            .target("my_mod")
            .file(Some("mod.rs"))
            .line(Some(123))
            .build();

        // This will call our mock wasm_log
        logger.log(&record);
    }
}
