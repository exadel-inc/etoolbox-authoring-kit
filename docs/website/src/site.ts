import {
  ESLTabs,
  ESLTab,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLToggleableDispatcher,
  ESLImage,
} from "@exadel/esl";
import {EAKSidebar} from "./navigation/navigation";
import {EAKBanner} from "./landing/banner/banner";

ESLTabs.register();
ESLTab.register();
ESLToggleableDispatcher.init();
ESLPanelGroup.register();
ESLPanel.register();
ESLTrigger.register();
ESLScrollbar.register();
ESLImage.register();

EAKBanner.register();
EAKSidebar.register();
