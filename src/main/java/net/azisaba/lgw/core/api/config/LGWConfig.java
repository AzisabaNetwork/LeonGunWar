package net.azisaba.lgw.core.api.config;

import de.exlll.configlib.Configuration;

@Configuration
public class LGWConfig {
    public DatabaseConfig database = new DatabaseConfig();

    @Configuration
    public static class DatabaseConfig {
        public String host = "127.0.0.1";
        public int port = 3306;
        public String database = "leongunwar";
        public String username = "lgwuser";
        public String password = "lgwpass";
    }
}
