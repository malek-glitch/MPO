// Condition badge styling — ported from the old web/js/api.js CONDITION_CLASS map.
import { CONDITION_LABEL, type ProductDto } from '@pcecom/shared';

export const CONDITION_CLASS: Record<string, string> = {
	like_new: 'c-comme-neuf',
	very_good: 'c-tres-bon',
	good: 'c-bon',
	fair: 'c-correct'
};

export function conditionInfo(p: Pick<ProductDto, 'isUsed' | 'used'>) {
	if (!p.isUsed) return { label: 'Neuf', dotClass: '' };
	const condition = p.used?.condition;
	return {
		label: (condition && CONDITION_LABEL[condition]) || 'Occasion',
		dotClass: (condition && CONDITION_CLASS[condition]) || 'c-correct'
	};
}
