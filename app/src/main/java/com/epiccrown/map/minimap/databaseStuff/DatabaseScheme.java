package com.epiccrown.map.minimap.databaseStuff;

class DatabaseScheme {
    static class FavsTable {
        static String NAME = "Favourites";

        public static class Cols {
            public static String Username = "username";
        }
    }

    static class HistoryTable {
        static String NAME = "History";

        public static class Cols {
            public static String Username = "username";
        }
    }
}
