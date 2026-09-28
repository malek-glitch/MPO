// Ported verbatim from web/admin.html's spec-preset constants.

export const APPLE_SPECS = {
	os: ['macOS Tahoe 26', 'macOS Sequoia 15', 'macOS Sonoma 14', 'macOS Ventura 13', 'macOS Monterey 12'],
	cpu: [
		'Apple M1',
		'Apple M1 Pro',
		'Apple M1 Max',
		'Apple M2',
		'Apple M2 Pro',
		'Apple M2 Max',
		'Apple M3',
		'Apple M3 Pro',
		'Apple M3 Max',
		'Apple M4',
		'Apple M4 Pro',
		'Apple M4 Max',
		'Apple M5',
		'Intel Core i5',
		'Intel Core i7'
	],
	ram: [8, 16, 18, 24, 32, 36, 48, 64],
	screen: [13.3, 13.6, 14.2, 15.3, 16.2],
	gpu: ['GPU intégré Apple'],
	storageType: 'SSD'
};

export const PC_SPECS = {
	os: ['Windows 11 Pro', 'Windows 11 Famille', 'Windows 10 Pro', 'Sans système (FreeDOS)', 'Linux (Ubuntu)'],
	cpu: [
		'Intel Core i3',
		'Intel Core i5',
		'Intel Core i7',
		'Intel Core i9',
		'Intel Core Ultra 5',
		'Intel Core Ultra 7',
		'Intel Core Ultra 9',
		'AMD Ryzen 3',
		'AMD Ryzen 5',
		'AMD Ryzen 7',
		'AMD Ryzen 9',
		'Intel Celeron',
		'Intel Pentium'
	],
	ram: [4, 8, 12, 16, 24, 32, 64],
	screen: [13.3, 14, 15.6, 16, 17.3, 18],
	gpu: [
		'Intégrée',
		'Intel Iris Xe',
		'Intel UHD',
		'AMD Radeon',
		'RTX 3050 4 Go',
		'RTX 3050 6 Go',
		'RTX 4050 6 Go',
		'RTX 4060 8 Go',
		'RTX 4070 8 Go',
		'RTX 4080 12 Go',
		'RTX 4090 16 Go',
		'RTX 5060 8 Go',
		'RTX 5070 8 Go',
		'RTX 5080 16 Go',
		'RTX 5090 24 Go'
	],
	storageType: 'SSD NVMe'
};

export const CHROMEOS_BRANDS = ['acer', 'hp', 'lenovo', 'asus', 'samsung'];
export const GAMING_BRANDS = ['msi', 'razer', 'alienware'];
export const COMMON_BRANDS = ['Apple', 'Dell', 'HP', 'Lenovo', 'Asus', 'Acer', 'MSI'];
export const MORE_BRANDS = [
	'Microsoft',
	'Samsung',
	'Huawei',
	'Razer',
	'Gigabyte',
	'Toshiba',
	'Alienware',
	'Fujitsu',
	'Xiaomi',
	'Montage'
];
export const STORAGE_VALUES = [128, 256, 512, 1024, 2048];
export const WARRANTY_VALUES = [0, 3, 6, 12, 24];
export const OS_OTHER = '__other';
