import { ref, type Ref } from 'vue';

export const useAbortController = () => {
  const abortController: Ref<AbortController | null> = ref(null);

  const createController = (): AbortController => {
    if (abortController.value) {
      abortController.value.abort();
    }

    const controller = new AbortController();
    abortController.value = controller;
    return controller;
  };

  const getSignal = (controller: AbortController): AbortSignal => {
    return controller.signal;
  };

  const isCurrentController = (controller: AbortController): boolean => {
    return abortController.value === controller;
  };

  const clearController = (controller: AbortController): void => {
    if (abortController.value === controller) {
      abortController.value = null;
    }
  };

  return {
    abortController,
    createController,
    getSignal,
    isCurrentController,
    clearController,
  };
};
