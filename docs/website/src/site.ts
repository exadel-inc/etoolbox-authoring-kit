import {
  ESLTabs,
  ESLTab,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLToggleableDispatcher,
} from '@exadel/esl';
import {ESLDemoSidebar} from './navigation/navigation';
import {EAKBannerLogo} from './landing/banner-logo/banner-logo';

ESLDemoSidebar.register();
EAKBannerLogo.register();

ESLTabs.register();
ESLTab.register();
ESLToggleableDispatcher.init();
ESLPanelGroup.register();
ESLPanel.register();
ESLTrigger.register();
ESLScrollbar.register();
