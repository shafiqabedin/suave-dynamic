package suave;

public interface StateEnums {
    // NOTE: These types are going to be mostly domain dependent, and
    // not so much visual... except sometimes it'll SEEM like they
    // are.  I.e. I may draw a line and use that to indicate a path,
    // but that doesn't mean it's just a line...
    //
    // Hmm, really I think they break down into two categories,
    // "things from the simulator" and "things the user made via the
    // interface, i.e. lines, areas, points, targets" - except that
    // some of those user things might be supplied from elsewhere too.
    // Blah.

    enum StateType {

        UNKNOWN("Unknown"),
        SPHERE("Sphere"),
        LINE("Line"),
        UNIT("Unit"),
        UAV("UAV"),
        TARGET_MARKER("TargetMarker"),
        ATR("ATR");
        private String name;

        StateType(String n) {
            this.name = n;
        }

        public String toString() {
            return this.name;
        }
    };

    enum ForceID {

        UNKNOWN("Unknown"),
        BLUEFOR("Bluefor"),
        OPFOR("Opfor"),
        NEUTRAL("Neutral"),
        CIVILIAN("Civilian"),
        WHITE("White"),
        RED("Red"),
        BLACK("Black"),
        BLUE("Blue"),
        GREEN("Green"),
        COLOR0("Color0"),
        COLOR1("Color1"),
        COLOR2("Color2"),
        COLOR3("Color3"),
        COLOR4("Color4"),
        COLOR5("Color5"),
        COLOR6("Color6"),
        COLOR7("Color7"),
        COLOR8("Color8"),
        COLOR9("Color9"),
        COLOR10("Color10"),
        COLOR11("Color11"),
        COLOR12("Color12"),
        COLOR13("Color13"),
        COLOR14("Color14"),
        COLOR15("Color15"),
        COLOR16("Color16"),
        COLOR17("Color17"),
        COLOR18("Color18"),
        COLOR19("Color19"),
        COLOR20("Color20"),
        COLOR21("Color21"),
        COLOR22("Color22"),
        COLOR23("Color23");
        private String name;

        ForceID(String n) {
            this.name = n;
        }

        public String toString() {
            return this.name;
        }
    };

    enum KillStatus {

        UNKNOWN("Unknown"),
        LIVE("Live"),
        DEAD("Dead");
        private String name;

        KillStatus(String n) {
            this.name = n;
        }

        public String toString() {
            return this.name;
        }
    };
}
