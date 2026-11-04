import {FormBuilder} from '@angular/forms';
import {MatDialogRef} from '@angular/material/dialog';
import {HttpErrorResponse} from '@angular/common/http';
import {TranslocoService} from '@jsverse/transloco';
import {of, throwError} from 'rxjs';
import {AgencyStopsService} from '../../../generated/public-transport-api';
import {AgencyStorageService} from '../../../auth/agency-storage.service';
import {NotificationService} from '../../../shared/notification.service';
import {
    AgencyStopNameDialogData,
    AgencyStopNameDialogResult,
    AgencyStopNameModalComponent
} from './agency-stop-name-modal.component';

describe('AgencyStopNameModalComponent', () => {
    const data: AgencyStopNameDialogData = {stopId: 10, name: 'Rynek', lat: 52.23, lon: 21.01};

    function modal(update = jasmine.createSpy('updateAgencyStopName').and.returnValue(of({}))) {
        const close = jasmine.createSpy('close');
        const showSuccess = jasmine.createSpy('showSuccess');
        const showError = jasmine.createSpy('showError');
        const component = new AgencyStopNameModalComponent(
            {close, afterOpened: () => of(undefined)} as unknown as MatDialogRef<AgencyStopNameModalComponent, AgencyStopNameDialogResult>,
            data,
            new FormBuilder(),
            {updateAgencyStopName: update} as unknown as AgencyStopsService,
            {getInstance: () => 'WAWA'} as AgencyStorageService,
            {showSuccess, showError} as unknown as NotificationService,
            {translate: (key: string) => key} as TranslocoService
        );
        return {component, close, update, showSuccess, showError};
    }

    it('saves the trimmed name for the agency and closes with it', () => {
        const {component, close, update, showSuccess} = modal();

        component.form.controls.name.setValue('  Dworzec  ');
        component.save();

        expect(update).toHaveBeenCalledWith('WAWA', 10, {name: 'Dworzec'});
        expect(showSuccess).toHaveBeenCalledWith('agency.stops.used.edit.saved');
        expect(close).toHaveBeenCalledWith({stopId: 10, name: 'Dworzec'});
    });

    it('rejects a blank name without calling the agency rename', () => {
        const {component, update, close} = modal();

        component.form.controls.name.setValue('   ');
        component.save();

        expect(update).not.toHaveBeenCalled();
        expect(close).not.toHaveBeenCalled();
        expect(component.form.controls.name.hasError('required')).toBeTrue();
    });

    it('keeps the dialog open when the rename fails', () => {
        const update = jasmine.createSpy('updateAgencyStopName').and.returnValue(throwError(() => new HttpErrorResponse({
            error: {message: 'Nieznany przystanek'},
            status: 400
        })));
        const {component, close, showError} = modal(update);

        component.save();

        expect(close).not.toHaveBeenCalled();
        expect(showError).toHaveBeenCalledWith('Nieznany przystanek');
        expect(component.saving).toBeFalse();
    });
});
