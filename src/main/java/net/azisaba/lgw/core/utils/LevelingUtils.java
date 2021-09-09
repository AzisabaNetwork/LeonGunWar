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
    private static final String PRESTIGE_COLOR_MORE = ChatColor.DARK_RED + "";
    //TODO 続く...
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

}
