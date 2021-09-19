package net.azisaba.lgw.core.utils;

import org.bukkit.ChatColor;

public class LevelingUtils {

    // TODO xpとレベルの計算、angelOfDeathLevelから確率を産出

    ///////////////////////////////////////////////////////////////
    //REQUIRED XPS
    ///////////////////////////////////////////////////////////////

    //TODO ここはconfigから数値を持ってくるようにする。

    private static final int REQUIRED_XPS_ONE = 0;
    private static final int REQUIRED_XPS_TWO = 20;
    private static final int REQUIRED_XPS_THREE = 50;
    private static final int REQUIRED_XPS_FOUR = 80;
    private static final int REQUIRED_XPS_FIVE = 100;
    private static final int REQUIRED_XPS_SIX = 250;
    private static final int REQUIRED_XPS_SEVEN = 500;
    private static final int REQUIRED_XPS_EIGHT = 1000;
    private static final int REQUIRED_XPS_NINE = 2500;
    private static final int REQUIRED_XPS_TEN = 5000;
    private static final int REQUIRED_XPS_ELEVEN = 10000;
    private static final int REQUIRED_XPS_TWELVE = 15000;
    private static final int REQUIRED_XPS_MORE = 15000;

    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    //REQUIRED TOTAL XPS
    ///////////////////////////////////////////////////////////////

    /*
    private static final int REQUIRED_TOTAL_XPS_ONE = 0;
    private static final int REQUIRED_TOTAL_XPS_TWO = 20;
    private static final int REQUIRED_TOTAL_XPS_THREE = 70;
    private static final int REQUIRED_TOTAL_XPS_FOUR = 150;
    private static final int REQUIRED_TOTAL_XPS_FIVE = 250;
    private static final int REQUIRED_TOTAL_XPS_SIX = 500;
    private static final int REQUIRED_TOTAL_XPS_SEVEN = 1000;
    private static final int REQUIRED_TOTAL_XPS_EIGHT = 2000;
    private static final int REQUIRED_TOTAL_XPS_NINE = 4500;
    private static final int REQUIRED_TOTAL_XPS_TEN = 9500;
    private static final int REQUIRED_TOTAL_XPS_ELEVEN = 19500;

     */
    private static final int REQUIRED_TOTAL_XPS_TWELVE = 34500;

    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    //LEVEL COLOR
    ///////////////////////////////////////////////////////////////

    private static final String PRESTIGE_COLOR_ONE_TO_FOUR = ChatColor.GRAY + "";
    private static final String PRESTIGE_COLOR_FIVE_TO_NINE = ChatColor.WHITE + "";
    private static final String PRESTIGE_COLOR_TEN_TO_FOURTEEN = ChatColor.GOLD + "";
    private static final String PRESTIGE_COLOR_FIFTEEN_TO_NINETEEN = ChatColor.GREEN + "";
    private static final String PRESTIGE_COLOR_TWENTY_TO_TWENTY_FOUR = ChatColor.AQUA + "";
    private static final String PRESTIGE_COLOR_TWENTY_FIVE_TO_TWENTY_NINE = ChatColor.DARK_GREEN + "";
    private static final String PRESTIGE_COLOR_THIRTY_TO_THIRTY_FOUR = ChatColor.LIGHT_PURPLE + "";
    private static final String PRESTIGE_COLOR_THIRTY_FIVE_TO_THIRTY_NINE = ChatColor.RED + "";
    private static final String PRESTIGE_COLOR_FORTY_TO_FORTY_FOUR = ChatColor.DARK_AQUA + "";
    private static final String PRESTIGE_COLOR_FORTY_FIVE_TO_FORTY_NINE = ChatColor.DARK_RED + "";
    private static final String PRESTIGE_COLOR_FIFTY_TO_FIFTY_FOUR = ChatColor.DARK_BLUE + "";
    private static final String PRESTIGE_COLOR_FIFTY_FIVE_TO_FIFTY_NINE = ChatColor.DARK_PURPLE + "";
    private static final String PRESTIGE_COLOR_SIXTY_TO_NINETY_NINE = "[RAINBOW]";
    private static final String PRESTIGE_COLOR_MORE = "[RAINBOW_BOLDED]";

    ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////
    //ANGEL OF DEATH PERCENTAGE
    //////////////////////////////////////////////////////////////

    //TODO ここはconfigから数値を持ってくるようにする。

    private static final double ANGEL_NONE = 0;
    private static final double ANGEL_ONE = 1;
    private static final double ANGEL_TWO = 2;
    private static final double ANGEL_THREE = 3;
    private static final double ANGEL_FOUR = 4;
    private static final double ANGEL_FIVE = 5;
    private static final double ANGEL_SIX = 6;
    private static final double ANGEL_SEVEN = 7;
    private static final double ANGEL_EIGHT = 8;
    private static final double ANGEL_NINE = 9;
    private static final double ANGEL_TEN = 10;
    private static final double ANGEL_ELEVEN = 11;
    private static final double ANGEL_TWELVE = 12;
    ///////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////////////
    //ANGEL OF DEATH PERCENTAGE
    //////////////////////////////////////////////////////////////

    //TODO ここはconfigから数値を持ってくるようにする。

    private static final String ANGEL_ICON_NONE = "⭐";
    private static final String ANGEL_ICON_ONE = "✧";
    private static final String ANGEL_ICON_TWO = "✤";
    private static final String ANGEL_ICON_THREE = "⚝";
    private static final String ANGEL_ICON_FOUR = "❄";
    private static final String ANGEL_ICON_FIVE = "✻";
    private static final String ANGEL_ICON_SIX = "❂";
    private static final String ANGEL_ICON_SEVEN = "✺";
    private static final String ANGEL_ICON_EIGHT = "❤";
    private static final String ANGEL_ICON_NINE = "✌";
    private static final String ANGEL_ICON_TEN = "❀";
    private static final String ANGEL_ICON_ELEVEN = "犬";
    private static final String ANGEL_ICON_TWELVE = "ඞ";

    ///////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////
    //ANGEL OF DEATH REQUIRED COIN
    ///////////////////////////////////////////////////////////////
    private static final int REQUIRED_ANGEL_NONE = 0;
    private static final int REQUIRED_ANGEL_ONE = 10000;
    private static final int REQUIRED_ANGEL_TWO = 40000;
    private static final int REQUIRED_ANGEL_THREE = 100000;
    private static final int REQUIRED_ANGEL_FOUR = 200000;
    private static final int REQUIRED_ANGEL_FIVE = 400000;
    private static final int REQUIRED_ANGEL_SIX = 1000000;
    private static final int REQUIRED_ANGEL_SEVEN = 2000000;
    private static final int REQUIRED_ANGEL_EIGHT = 3000000;
    private static final int REQUIRED_ANGEL_NINE = 4000000;
    private static final int REQUIRED_ANGEL_TEN = 6000000;
    private static final int REQUIRED_ANGEL_ELEVEN = 8000000;
    private static final int REQUIRED_ANGEL_TWELVE = 10000000;
    ///////////////////////////////////////////////////////////////

    public static int getRequiredXpsTotal(int level){

        int total = 0;

        for(int i = level; i > 0; i--){
            total = total + getRequiredXpsNextLevel(i);
        }

        return total;

    }

    public static int getRequiredXpsNextLevel(int level){

        if(level > 12){
            return REQUIRED_XPS_MORE;
        }

        switch ( level ){

            case 1 : return REQUIRED_XPS_ONE;
            case 2 : return REQUIRED_XPS_TWO;
            case 3 : return REQUIRED_XPS_THREE;
            case 4 : return REQUIRED_XPS_FOUR;
            case 5 : return REQUIRED_XPS_FIVE;
            case 6 : return REQUIRED_XPS_SIX;
            case 7 : return REQUIRED_XPS_SEVEN;
            case 8 : return REQUIRED_XPS_EIGHT;
            case 9 : return REQUIRED_XPS_NINE;
            case 10: return REQUIRED_XPS_TEN;
            case 11: return REQUIRED_XPS_ELEVEN;
            case 12 : return REQUIRED_XPS_TWELVE;
            default: return 0;
        }

    }

    public static int getLevelFromXp(int xps){

        if( REQUIRED_TOTAL_XPS_TWELVE > xps ){

            for(int i = 1; i <= 12; i++){

                if(getRequiredXpsTotal(i) > xps){

                    return i - 1;

                }

            }

        }else{

            return ((xps - REQUIRED_TOTAL_XPS_TWELVE) / REQUIRED_XPS_MORE ) + 12;

        }


        return 0;

    }

    public static String coloring(int level,String prefix){

        int prestige = level / 5;

        switch ( prestige ){
            case 0: return PRESTIGE_COLOR_ONE_TO_FOUR;
            case 1: return PRESTIGE_COLOR_FIVE_TO_NINE;
            case 2: return PRESTIGE_COLOR_TEN_TO_FOURTEEN;
            case 3: return PRESTIGE_COLOR_FIFTEEN_TO_NINETEEN;
            case 4: return PRESTIGE_COLOR_TWENTY_TO_TWENTY_FOUR;
            case 5: return PRESTIGE_COLOR_TWENTY_FIVE_TO_TWENTY_NINE;
            case 6: return PRESTIGE_COLOR_THIRTY_TO_THIRTY_FOUR;
            case 7: return PRESTIGE_COLOR_THIRTY_FIVE_TO_THIRTY_NINE;
            case 8: return PRESTIGE_COLOR_FORTY_TO_FORTY_FOUR;
            case 9: return PRESTIGE_COLOR_FORTY_FIVE_TO_FORTY_NINE;
            case 10: return PRESTIGE_COLOR_FIFTY_TO_FIFTY_FOUR;
            case 11: return PRESTIGE_COLOR_FIFTY_FIVE_TO_FIFTY_NINE;
        }

        if(level >= 60){
            if(level < 100){

                String colored = null;
                int count = 0;

                for ( char c : prefix.toCharArray() ) {
                    colored = colored + getColor(count) + c;
                    count++;
                }

                return colored;

            }else {

                String colored = null;
                int count = 0;

                for ( char c : prefix.toCharArray() ) {
                    colored = colored + getColor(count) + "" + ChatColor.BOLD + c;
                    count++;
                }

                return colored;

            }
        }

        return null;

    }

    private static ChatColor getColor(int count){

        count = count % 7;

        switch ( count ){
            case 0: return ChatColor.RED;
            case 1: return ChatColor.GOLD;
            case 2: return ChatColor.YELLOW;
            case 3: return ChatColor.GREEN;
            case 4: return ChatColor.AQUA;
            case 5: return ChatColor.LIGHT_PURPLE;
            case 6: return ChatColor.DARK_PURPLE;
            default: return ChatColor.WHITE;
        }

    }

    public static double getAngelOfDeathPercentage(int level){

        //TODO これでいいのだろうか

        switch ( level ){
            case 0 : return ANGEL_NONE;
            case 1 : return ANGEL_ONE;
            case 2 : return ANGEL_TWO;
            case 3 : return ANGEL_THREE;
            case 4 : return ANGEL_FOUR;
            case 5 : return ANGEL_FIVE;
            case 6 : return ANGEL_SIX;
            case 7 : return ANGEL_SEVEN;
            case 8 : return ANGEL_EIGHT;
            case 9 : return ANGEL_NINE;
            case 10: return ANGEL_TEN;
            case 11: return ANGEL_ELEVEN;
            case 12 : return ANGEL_TWELVE;
            default: return 0D;
        }

    }

    public static int getRequiredCoin(int level){

        switch ( level ){
            case 0 : return REQUIRED_ANGEL_NONE;
            case 1 : return REQUIRED_ANGEL_ONE;
            case 2 : return REQUIRED_ANGEL_TWO;
            case 3 : return REQUIRED_ANGEL_THREE;
            case 4 : return REQUIRED_ANGEL_FOUR;
            case 5 : return REQUIRED_ANGEL_FIVE;
            case 6 : return REQUIRED_ANGEL_SIX;
            case 7 : return REQUIRED_ANGEL_SEVEN;
            case 8 : return REQUIRED_ANGEL_EIGHT;
            case 9 : return REQUIRED_ANGEL_NINE;
            case 10: return REQUIRED_ANGEL_TEN;
            case 11: return REQUIRED_ANGEL_ELEVEN;
            case 12 : return REQUIRED_ANGEL_TWELVE;
            default: return 0;
        }

    }

    public static String getAngelIcon(int angelLevel){

        switch ( angelLevel ){
            case 0 : return ANGEL_ICON_NONE;
            case 1 : return ANGEL_ICON_ONE;
            case 2 : return ANGEL_ICON_TWO;
            case 3 : return ANGEL_ICON_THREE;
            case 4 : return ANGEL_ICON_FOUR;
            case 5 : return ANGEL_ICON_FIVE;
            case 6 : return ANGEL_ICON_SIX;
            case 7 : return ANGEL_ICON_SEVEN;
            case 8 : return ANGEL_ICON_EIGHT;
            case 9 : return ANGEL_ICON_NINE;
            case 10: return ANGEL_ICON_TEN;
            case 11: return ANGEL_ICON_ELEVEN;
            case 12 : return ANGEL_ICON_TWELVE;
            default: return "";
        }

    }

}
