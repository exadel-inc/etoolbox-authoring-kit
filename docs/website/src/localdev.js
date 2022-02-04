import {
  ESLTabs,
  ESLTab,
  ESLPopup,
  ESLPanel,
  ESLPanelGroup,
  ESLTrigger,
  ESLScrollbar,
  ESLAlert,
  ESLToggleableDispatcher,
} from '@exadel/esl';
import { ESLDemoSidebar } from './navigation/navigation';

ESLDemoSidebar.register();

ESLTabs.register();
ESLTab.register();

ESLToggleableDispatcher.init();
ESLPopup.register();

ESLPanelGroup.register();
ESLPanel.register();

ESLTrigger.register();

ESLScrollbar.register();

ESLAlert.init({
  closeOnOutsideAction: true
});
