import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarsEditorComponent } from './calendars-editor.component';

describe('CalendarsEditorComponent', () => {
  let component: CalendarsEditorComponent;
  let fixture: ComponentFixture<CalendarsEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CalendarsEditorComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(CalendarsEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
