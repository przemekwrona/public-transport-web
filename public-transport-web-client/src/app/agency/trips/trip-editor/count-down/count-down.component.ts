import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {finalize, map, Observable, Subscription, takeWhile, timer} from "rxjs";
import {CommonModule} from "@angular/common";

@Component({
    selector: 'app-count-down',
    imports: [
        CommonModule
    ],
    templateUrl: './count-down.component.html',
    styleUrl: './count-down.component.scss'
})
export class CountDownComponent implements OnInit {
    // 1. Store the initial duration (e.g., 30 seconds)
    @Input() initialSeconds = 30;
    @Input() forceRefresh: Observable<boolean>;

    // 2. The current duration (used in the map function)
    private seconds: number = this.initialSeconds;

    // 3. Store the active subscription
    private timerSubscription: Subscription | undefined;

    // 4. The Observable stream itself (optional, but good practice)
    public timeRemaining$: Observable<number>;

    // Assuming 'onTime' is an EventEmitter or a Subject for side effects
    @Output() onTime = new EventEmitter<void>();

    ngOnInit(): void {
        this.forceRefresh.subscribe(isRefresh => {
            if (isRefresh) {
                this.resetAndStartTimer();
            }
        })
    }

    startTimer() {
        // 1. Clear any existing timer before starting a new one
        this.resetTimer();

        // 2. Set the initial seconds for the new countdown
        this.seconds = this.initialSeconds;

        this.timeRemaining$ = timer(0, 1000).pipe(
            map(n => (this.seconds - n) * 1000), // n starts at 0, 1, 2...
            takeWhile(n => n >= 0),
            finalize(() => {
            })
        );

        // 3. Subscribe and store the subscription
        this.timerSubscription = this.timeRemaining$.subscribe(time => {
            // Optional: Update a property for display (e.g., this.displayTime = time)
            if (time === 0) {
                this.onTime.emit();
            }
        });
    }

    resetTimer() {
        // 1. Unsubscribe to immediately stop the running timer/stream.
        if (this.timerSubscription) {
            this.timerSubscription.unsubscribe();
            this.timerSubscription = undefined;
        }

        // 2. Reset the displayed time/state in the component (if necessary)
        this.seconds = this.initialSeconds;
    }

    resetAndStartTimer() {
        this.resetTimer();
        this.startTimer();
    }

}
