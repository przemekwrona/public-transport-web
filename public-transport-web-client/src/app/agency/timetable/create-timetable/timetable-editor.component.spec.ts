import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TimetableEditorComponent } from './timetable-editor.component';

describe('CreateTimetableComponent', () => {
  let component: TimetableEditorComponent;
  let fixture: ComponentFixture<TimetableEditorComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TimetableEditorComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TimetableEditorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
