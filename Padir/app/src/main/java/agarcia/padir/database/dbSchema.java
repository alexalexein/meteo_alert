package agarcia.padir.database;

/**
 * Created by agarcia on 03/12/2017.
 */

public class dbSchema {
    public static final class dbTable{
        public static final String NAME = "alarmsTable";

        public static final class Cols{
            public static final String UUID = "uuid";
            public static final String LOCATION = "location";
            public static final String TIME_OF_DAY = "timeOfDay";
            public static final String FORECAST_TYPE = "forecastType";
        }
    }
}
