import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {of} from 'rxjs';
import {BrigadeTimetableService} from '../../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../../auth/agency-storage.service';
import {BrigadeTimetableModalComponent} from './brigade-timetable-modal.component';

describe('BrigadeTimetableModalComponent', () => {
    let component: BrigadeTimetableModalComponent;
    let fixture: ComponentFixture<BrigadeTimetableModalComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [BrigadeTimetableModalComponent],
            providers: [
                {provide: MAT_DIALOG_DATA, useValue: {brigadeCode: '01', calendarCode: '0001', calendarSymbol: 'A'}},
                {provide: AgencyStorageService, useValue: {getInstance: () => 'test'}},
                {
                    provide: BrigadeTimetableService,
                    useValue: {getTimetableByBrigadeAndCalendarSymbol: () => of({trips: []})}
                }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(BrigadeTimetableModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });
});
