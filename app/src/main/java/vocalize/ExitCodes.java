package vocalize;

/**
 * Created by AMAGNONI on 20/03/2018.
 */

public enum ExitCodes {
    GOBACK(-2),
    SEVERE(-1),
    SUCCESS(0),
    REWIND(1),
    FINETURNO(2),
    NEXTRIGA(3),
    DISCONNECT(4),
    NAVIGATE(5),
    PICKING(6),
    STOCCAGGIO(7),
    RIPRISTINO(8),
    REWIND_SPECIAL(9),
    PARTIAL(10),
    RESET(11);

    private int value;

    ExitCodes(int value) {
        this.value = value;
    }

    public boolean equals(int value){
        return this.value == value;
    }

    public int getValue() {
        return value;
    }

}
