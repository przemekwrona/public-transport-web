import {Component, forwardRef, Input} from '@angular/core';
import moment from "moment";
import {ControlValueAccessor, NG_VALUE_ACCESSOR} from "@angular/forms";
import {animate, style, transition, trigger} from "@angular/animations";

@Component({
    selector: 'app-calendar-input',
    templateUrl: './calendar-input.component.html',
    styleUrl: './calendar-input.component.scss',
    animations: [
        trigger('fadeInOut', [
            transition(':enter', [
                style({ opacity: 0 }),
                animate('500ms ease-in', style({ opacity: 1 }))
            ]),
            transition(':leave', [
                animate('500ms ease-out', style({ opacity: 0 }))
            ])
        ])
    ],
    providers: [
        {
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => CalendarInputComponent),
            multi: true
        }
    ],
    standalone: false
})
export class CalendarInputComponent implements ControlValueAccessor {

    @Input() showNavigation: boolean = false;

    onChange = (value: string) => {};
    onTouched = () => {};

    public isFocus: boolean = false;

    public selectedDate: string = moment().format('YYYY-MM-DD');

    public includeDays: Set<string> = new Set<string>;

    public onChangeIncludeDays($event: Set<string>) {
        this.includeDays = $event;
        this.selectedDate = [...$event][0];
        this.onChange(this.selectedDate)
    }

    registerOnChange(fn: (value: string) => void): void {
        this.onChange = fn;
    }

    registerOnTouched(fn: () => void): void {
        this.onTouched = fn;
    }

    writeValue(value: string): void {
        this.selectedDate = value;
        this.includeDays = new Set<string>();
        this.includeDays.add(this.selectedDate);
    }

    onInput(event: Event): void {
        const inputValue = (event.target as HTMLInputElement).value;

        if(moment(inputValue, 'YYYY-MM-DD', true).isValid()) {
            this.selectedDate = inputValue;

            const currentIncludeDays = new Set<string>();
            currentIncludeDays.add(this.selectedDate);
            this.includeDays = currentIncludeDays;
        }

        this.onChange(inputValue);
    }
}
