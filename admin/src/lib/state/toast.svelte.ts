class ToastState {
	message = $state<string | null>(null);
	private timer: ReturnType<typeof setTimeout> | undefined;

	show(msg: string) {
		this.message = msg;
		clearTimeout(this.timer);
		this.timer = setTimeout(() => (this.message = null), 2500);
	}
}

export const toast = new ToastState();
