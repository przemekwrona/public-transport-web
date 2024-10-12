import {ChangeDetectionStrategy, Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from "@angular/material/dialog";
import {JourneySummaryResponse} from "../../../../generated/public-transport";

@Component({
    selector: 'app-summary-journey',
    templateUrl: './summary-journey.component.html',
    styleUrl: './summary-journey.component.scss',
    changeDetection: ChangeDetectionStrategy.OnPush
})
export class SummaryJourneyComponent {
    constructor(@Inject(MAT_DIALOG_DATA) public summary: JourneySummaryResponse) {
    }

    public isPositive(value: number | undefined): boolean {
        if (value == undefined) {
            return false;
        }
        return value > 0;
    }

    public isNegative(value: number | undefined): boolean {
        if (value == undefined) {
            return false;
        }
        return value < 0;
    }
}
