import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {of} from 'rxjs';
import {CalendarService} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';

import {BrigadeCreatorModalComponent} from './brigade-creator-modal.component';

describe('BrigadeCreatorModalComponent', () => {
    let component: BrigadeCreatorModalComponent;
    let fixture: ComponentFixture<BrigadeCreatorModalComponent>;
    let closeDialog: jasmine.Spy;

    beforeEach(async () => {
        closeDialog = jasmine.createSpy('close');

        TestBed.overrideComponent(BrigadeCreatorModalComponent, {
            set: {
                template: '',
                imports: [],
                providers: [
                    {provide: AgencyStorageService, useValue: {getInstance: () => 'agency-1'}},
                    {provide: CalendarService, useValue: {getCalendarItems: () => of({})}}
                ]
            }
        });

        await TestBed.configureTestingModule({
            imports: [ReactiveFormsModule, BrigadeCreatorModalComponent],
            providers: [
                {provide: MatDialogRef, useValue: {close: closeDialog}}
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(BrigadeCreatorModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('closes the dialog with the brigade to create', () => {
        component.modelForm.setValue({
            brigadeName: 'Poranna',
            defaultRouteCode: {routeCode: 'R1'},
            calendarId: {code: 'CAL'}
        });

        component.createBrigade();

        expect(closeDialog).toHaveBeenCalledWith(jasmine.objectContaining({
            brigadeName: 'Poranna',
            calendarCode: 'CAL',
            selectedRouteCode: 'R1'
        }));
    });

    it('keeps the dialog open when the form is incomplete', () => {
        component.createBrigade();

        expect(closeDialog).not.toHaveBeenCalled();
    });
});
