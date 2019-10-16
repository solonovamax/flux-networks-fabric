package fluxnetworks.api;

import fluxnetworks.FluxTranslate;
import fluxnetworks.common.core.Localization;

public enum EnumNavigationTabs {

    TAB_HOME(FluxTranslate.TAB_HOME),
    TAB_SELECTION(FluxTranslate.TAB_SELECTION),
    TAB_WIRELESS(FluxTranslate.TAB_WIRELESS),
    TAB_CONNECTION(FluxTranslate.TAB_CONNECTION),
    TAB_STATISTICS(FluxTranslate.TAB_STATISTICS),
    TAB_MEMBER(FluxTranslate.TAB_MEMBER),
    TAB_SETTING(FluxTranslate.TAB_SETTING),
    TAB_CREATE(FluxTranslate.TAB_CREATE);

    Localization tabName;

    EnumNavigationTabs(Localization tabName) {
        this.tabName = tabName;
    }

    public String getTranslatedName(){
        return tabName.t();
    }
}
