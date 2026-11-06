import {AbstractControl, ValidatorFn} from '@angular/forms';

export function passwordChangeValidator(): ValidatorFn {
    return (group: AbstractControl) => {
        const current = group.get('currentPassword')?.value;
        const next = group.get('newPassword')?.value;
        const repeat = group.get('repeatedPassword')?.value;
        const errors: Record<string, boolean> = {};
        if (next && repeat && next !== repeat) {
            errors['passwordMismatch'] = true;
        }
        if (current && next && current === next) {
            errors['samePassword'] = true;
        }
        return Object.keys(errors).length ? errors : null;
    };
}
